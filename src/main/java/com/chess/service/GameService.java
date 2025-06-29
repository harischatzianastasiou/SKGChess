package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.core.moves.capturing.CapturingMove;
import com.chess.core.moves.noncapturing.PawnJumpMove;
import com.chess.core.player.CurrentPlayer;
import com.chess.core.player.Player;
import com.chess.core.Alliance;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.exception.UserAlreadyHasActiveGameException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.model.entity.User;
import com.chess.repository.GameRepository;
import com.chess.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.chess.util.CompressionUtil;

@Service
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GamePositionService gamePositionService;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(GameRepository gameRepository,
                      UserRepository userRepository, 
                      SimpMessagingTemplate messagingTemplate,
                      GamePositionService gamePositionService) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.gamePositionService = gamePositionService;
    }

    @Transactional
    public Game createGame(String username, String gameType, Integer timeControlMinutes, Boolean isRated, String customRules, String playerColor) {
        try {
            // Validate username
            if (username == null || username.trim().isEmpty()) {
                logger.error("Username is null or empty");
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            
            // Find user with pessimistic locking to prevent concurrent game creation
            User user = userRepository.findByUsernameWithLock(username)
                .orElseThrow(() -> new UserNotFoundException(username));
            
            // Check if the user has active game using the new method
            if (userRepository.existsActiveGameForUser(user.getId())) {
                throw new UserAlreadyHasActiveGameException("User already has an active game");
            }
            
            // Create game object
            Game game = new Game();
            
            // Handle player color assignment
            if (playerColor == null || playerColor.trim().isEmpty()) {
                playerColor = "white"; // Default to white if no color specified
            }
            
            if (playerColor.equals("white")) {
                game.setWhitePlayer(user);
            } else if (playerColor.equals("black")) {
                game.setBlackPlayer(user);
            } else if (playerColor.equals("random")) {
                // Randomly assign player to white or black
                boolean isWhite = Math.random() < 0.5;
                if (isWhite) {
                    game.setWhitePlayer(user);
                } else {
                    game.setBlackPlayer(user);
                }
            } else {
                // Default to white for any unrecognized color
                game.setWhitePlayer(user);
            }
            game.setGameType(Optional.ofNullable(gameType).orElse("standard"));
            game.setTimeControlMinutes(Optional.ofNullable(timeControlMinutes).orElse(10));
            game.setStatus(GameStatus.WAITING_FOR_OPPONENT.name());
            game.setCreatedAt(LocalDateTime.now());
            game.setIsPlayerTurn(com.chess.core.Alliance.WHITE);

            // Create game manager with current state
            if(game.getGameType().equals("standard")){
                try {
                    IBoard board = IBoard.createStandardBoard();
                    // Compress the board data before storing it
                    String serializedBoard = board.serialize();
                    String compressedBoard = CompressionUtil.compress(serializedBoard);
                    game.setBoard(compressedBoard);
                } catch (Exception e) {
                    logger.error("Error creating standard board: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to create standard board: " + e.getMessage(), e);
                }
            } else {
                logger.warn("Unsupported game type: {}", game.getGameType());
                throw new IllegalArgumentException("Unsupported game type: " + game.getGameType());
            }
            
            // Save the game first to get an ID
            game = gameRepository.save(game);
            
            // Store the initial position for move history (after game is saved)
            if(game.getGameType().equals("standard")){
                try {
                    IBoard board = IBoard.createStandardBoard();
                    gamePositionService.storeInitialPosition(game, board);
                    logger.info("Stored initial position for game {}", game.getId());
                } catch (Exception e) {
                    logger.error("Error storing initial position: {}", e.getMessage(), e);
                    // Don't fail the game creation if position storage fails
                }
            }
            
            // Return the game
            return game;
        } catch (Exception e) {
            logger.error("Error creating game", e);
            throw e; // Re-throw the exception to be handled by the controller
        }
    }

    @Transactional
    public Game joinGame(String gameId, String username) {
        try {
            // Find the joining user
            User joiningUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
            
            // Check if the user has active game
            List<Game> activeGames = getActiveGamesByUsername(username);
            if (!activeGames.isEmpty()) {
                throw new UserAlreadyHasActiveGameException("User already has an active game");
            }

            // Find the game
            Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

            // Check if game is already full
            if (game.getWhitePlayer() != null && game.getBlackPlayer() != null) {
                throw new IllegalStateException("Game is already full");
            }

            // Check if user is trying to join their own game
            if ((game.getWhitePlayer() != null && game.getWhitePlayer().getId().equals(joiningUser.getId())) ||
                (game.getBlackPlayer() != null && game.getBlackPlayer().getId().equals(joiningUser.getId()))) {
                throw new IllegalStateException("You cannot join your own game");
            }

            // Set the player to the available position
            if (game.getWhitePlayer() == null) {
                game.setWhitePlayer(joiningUser);
            } else {
                game.setBlackPlayer(joiningUser);
            }
            
            // Update game status to IN_PROGRESS
            game.setStatus(GameStatus.IN_PROGRESS.name());
            
            // Timer will be started when the inviter is redirected to the game page
            // This ensures the white player has their full time from the moment they can see the board
            
            // Save and return the updated game
            return gameRepository.save(game);
        } catch (Exception e) {
            logger.error("Error joining game", e);
            throw e; // Re-throw the exception to be handled by the controller
        }
    }

    public Game getGameById(String gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public List<Game> getActiveGamesByUsername(String username) {
        // Find the user first
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        
        // Use the existing method with the user's ID
        List<Game> userGames = gameRepository.findByWhitePlayerIdOrBlackPlayerId(user.getId(), user.getId());
        
        // Filter to only include games with IN_PROGRESS or CHECK status
        return userGames.stream()
            .filter(game -> game.getStatus().equals(GameStatus.IN_PROGRESS.name()) || 
                           game.getStatus().equals(GameStatus.CHECK.name()))
            .collect(Collectors.toList());
    }

    public List<Game> getLast6CheckmateGamesForUser(String userId) {
        // Get all games where the user is either the white or black player
        return gameRepository.findLast6CheckmateGamesByWhitePlayerIdOrBlackPlayerId(
            userId,
            userId,
            PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public List<Game> getLast6GamesForUser(String userId) {
        // Get all games where the user is either the white or black player (excluding waiting games)
        return gameRepository.findLast6GamesByWhitePlayerIdOrBlackPlayerId(
            userId,
            userId,
            PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public List<Game> getLastGameForUser(String userId) {
        return gameRepository.findLastGameByWhitePlayerIdOrBlackPlayerId(
            userId,
            userId,
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public int numOfUserGames(String userId) {
        int count = gameRepository.numOfUserGames(userId, userId);
        return count;
    }
    
    @Transactional
    public Game makeMove(String gameId, int sourceCoordinate, int targetCoordinate) {
        // Check if game exists
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        // Check if game has started by checking status
        if (game.getStatus().equals(GameStatus.WAITING_FOR_OPPONENT.name())) {
            throw new IllegalStateException("Cannot make moves until opponent joins");
        }
        
        // Check if game has ended (prevent moves after game end)
        if (game.getStatus().equals(GameStatus.TIME_OUT.name()) ||
            game.getStatus().equals(GameStatus.CHECKMATE.name()) ||
            game.getStatus().equals(GameStatus.DRAW.name()) ||
            game.getStatus().equals(GameStatus.RESIGNED.name()) ||
            game.getStatus().equals(GameStatus.STALEMATE.name()) ||
            game.getStatus().equals(GameStatus.THREEFOLD_REPETITION.name()) ||
            game.getStatus().equals(GameStatus.FIFTY_MOVE_RULE.name()) ||
            game.getStatus().equals(GameStatus.INSUFFICIENT_MATERIAL.name()) ||
            game.getStatus().equals(GameStatus.MUTUAL_AGREEMENT.name())) {
            throw new IllegalStateException("Cannot make moves in a game that has ended");
        }
        
        // Get current game state - DECOMPRESS the board before deserialization
        String decompressedBoard = CompressionUtil.safeDecompress(game.getBoard());
        IBoard currentBoard = IBoard.deserialize(decompressedBoard, game.getLastMoveData());
        
        // Find the move from legal moves
        logger.info("sourceCoordinate: {}", sourceCoordinate);
        logger.info("targetCoordinate: {}", targetCoordinate);

        Player player = currentBoard.getCurrentPlayer();
        Move move = player.getMoves().stream()
            .filter(m -> m.getSourceCoordinate() == sourceCoordinate 
                    && m.getTargetCoordinate() == targetCoordinate)
            .findFirst()
            .orElseThrow(() -> new InvalidMoveException("Invalid move"));
            
        // Execute the move
        com.chess.core.board.IBoard newBoard = move.execute();
        CurrentPlayer currentPlayer = (CurrentPlayer) newBoard.getCurrentPlayer();

        // Calculate time used for this move (if timer is enabled)
        double timeUsedSeconds = 0.0; // Changed to double for decimal precision
        if (game.getLastMoveAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            // Calculate time difference in milliseconds and convert to seconds with decimal precision
            long timeDiffMillis = java.time.Duration.between(game.getLastMoveAt(), now).toMillis();
            timeUsedSeconds = timeDiffMillis / 1000.0; // Convert to decimal seconds
        }

        // Update timer for the player who just moved
        if (game.getTimeControlMinutes() != null) {
            Alliance playerAlliance = move.getPieceToMove().getPieceAlliance();
            if (playerAlliance == Alliance.WHITE) {
                double newTime = game.getWhiteTimeLeftSeconds() - timeUsedSeconds; // Use double arithmetic
                game.setWhiteTimeLeftSeconds(Math.max(0.0, newTime)); // Ensure non-negative
            } else {
                double newTime = game.getBlackTimeLeftSeconds() - timeUsedSeconds; // Use double arithmetic
                game.setBlackTimeLeftSeconds(Math.max(0.0, newTime)); // Ensure non-negative
            }
            // Update last move time
            game.setLastMoveAt(LocalDateTime.now());
        }

        // Create move data object
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode moveData = objectMapper.createObjectNode();
        
        // Add common move properties
        moveData.put("sourceCoordinate", move.getSourceCoordinate());
        moveData.put("targetCoordinate", move.getTargetCoordinate());
        moveData.put("pieceSymbol", move.getPieceToMove().getPieceSymbol().toString());
        moveData.put("pieceAlliance", move.getPieceToMove().getPieceAlliance().toString());
        moveData.put("timeUsedSeconds", timeUsedSeconds); // Add time used to move data
        
        // Determine move type for sound effects
        if(move instanceof CapturingMove) {
            moveData.put("moveType", "CAPTURE");
        } else if(currentPlayer.isInCheck()) {
            moveData.put("moveType", "CHECK");
        } else if(currentPlayer.isCheckmate()) {
            moveData.put("moveType", "CHECKMATE");
        } else if(move instanceof PawnJumpMove) {
            moveData.put("moveType", "PAWN_JUMP");
        } else {
            moveData.put("moveType", "NORMAL");
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///   With each move a new currentBoard is created to represent the new state of the tiles and the turn of the next users.     //           
        ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
        ///   Also Current and Opponent user have changed.                                                                      //
        ///   The new current user gets the opposite color of the previous current user. Same applies for the opponent.       //
        ///   Tiles, current user and opponent user are created with the currentBoard, and are immutable afterwards.                 //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Update game status based on board state
        if (currentPlayer.isCheckmate()) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.CHECKMATE.name());
        } else if(currentPlayer.isDraw() == GameStatus.DRAW) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.DRAW.name());
        } else if(currentPlayer.isDraw() == GameStatus.STALEMATE) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.STALEMATE.name());
        } else if(currentPlayer.isDraw() == GameStatus.THREEFOLD_REPETITION) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.THREEFOLD_REPETITION.name());
        } else if(currentPlayer.isDraw() == GameStatus.FIFTY_MOVE_RULE) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.FIFTY_MOVE_RULE.name());
        } else if(currentPlayer.isDraw() == GameStatus.INSUFFICIENT_MATERIAL) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.INSUFFICIENT_MATERIAL.name());
        } else if(currentPlayer.isInCheck()) {
            game.setStatus(com.chess.model.entity.Game.GameStatus.CHECK.name());
        }
        
        try {
            // Store the serialized move data
            game.setLastMoveData(objectMapper.writeValueAsString(moveData));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize move data", e);
        }

        // Serialize and compress the new board to reduce storage costs
        String serializedBoard = newBoard.serialize();
        String compressedBoard = CompressionUtil.safeCompress(serializedBoard);
        
        // Log compression statistics for monitoring
        logger.info("Board compression: {}", CompressionUtil.getCompressionStats(serializedBoard, compressedBoard));
        
        // Store the compressed board
        game.setBoard(compressedBoard);

        // Increment move count each time a move is made
        game.setMoveCount(game.getMoveCount() + 1);
        game.setIsPlayerTurn(newBoard.getCurrentPlayer().getAlliance());
        
        // Store the position for move history viewing
        try {
            gamePositionService.storePosition(game, newBoard, move, game.getMoveCount());
            logger.info("Stored position for move {} in game {}", game.getMoveCount(), gameId);
        } catch (Exception e) {
            logger.error("Failed to store position for move history", e);
            // Don't fail the move if position storage fails
        }
        
        // Save the game (which will cascade to save the position)
        return gameRepository.save(game);
    }

    @Transactional
    public void deleteGame(String gameId) {
        // Find the game
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        // Check if game is still waiting for opponent
        if (!game.getStatus().equals(GameStatus.WAITING_FOR_OPPONENT.name())) {
            throw new IllegalStateException("Cannot delete a game that has already started");
        }
        
        // Delete the game
        gameRepository.delete(game);
    }

    /**
     * Handle timeout when a player runs out of time
     * @param gameId The ID of the game
     * @return The updated game with timeout status
     */
    @Transactional
    public Game handleTimeout(String gameId) {
        // Check if game exists
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        // Check if game is in progress or in check (allow timeout in both cases)
        if (!game.getStatus().equals(GameStatus.IN_PROGRESS.name()) && 
            !game.getStatus().equals(GameStatus.CHECK.name())) {
            throw new IllegalStateException("Cannot handle timeout for game that is not in progress or in check");
        }
        
        // Determine which player ran out of time based on current turn
        Alliance currentPlayerAlliance = game.getIsPlayerTurn();
        
        // The player whose turn it is when they run out of time is the one who loses
        // The opposite player wins
        if (currentPlayerAlliance == Alliance.WHITE) {
            // White ran out of time, so Black wins
            game.setWinner(game.getBlackPlayer());
        } else {
            // Black ran out of time, so White wins
            game.setWinner(game.getWhitePlayer());
        }
        
        // Set game status to TIME_OUT
        game.setStatus(GameStatus.TIME_OUT.name());
        
        // Save and return the updated game
        return gameRepository.save(game);
    }

    /**
     * Start the timer for a game when the inviter is redirected to the game page
     * This ensures the white player has their full time from the moment they can see the board
     * @param gameId The ID of the game
     * @return The updated game with timer started
     */
    @Transactional
    public Game startGameTimer(String gameId) {
        // Find the game
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        // Check if game is in progress or in check
        if (!game.getStatus().equals(GameStatus.IN_PROGRESS.name()) && 
            !game.getStatus().equals(GameStatus.CHECK.name())) {
            throw new IllegalStateException("Cannot start timer for game that is not in progress or in check");
        }
        
        // Check if timer is already started
        if (game.getLastMoveAt() != null) {
            logger.info("Timer already started for game: {}", gameId);
            return game;
        }
        
        // Initialize timer when inviter is redirected to game page
        if (game.getTimeControlMinutes() != null) {
            logger.info("Starting timer for game: {} when inviter is redirected", gameId);
            // Set initial time for both players (convert minutes to seconds with decimal precision)
            game.setWhiteTimeLeftSeconds(game.getTimeControlMinutes() * 60.0); // Use decimal precision
            game.setBlackTimeLeftSeconds(game.getTimeControlMinutes() * 60.0); // Use decimal precision
            // Set the last move time to now - timer starts ticking
            game.setLastMoveAt(LocalDateTime.now());
            
            // Save and return the updated game
            return gameRepository.save(game);
        }
        
        return game;
    }
}
