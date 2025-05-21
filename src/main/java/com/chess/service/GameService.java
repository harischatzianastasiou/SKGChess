package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.core.moves.capturing.CapturingMove;
import com.chess.core.player.CurrentPlayer;
import com.chess.core.player.Player;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.exception.UserAlreadyHasActiveGameException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.model.entity.User;
import com.chess.repository.GameRepository;
import com.chess.repository.UserRepository;
import com.chess.util.Sounduser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
@Service
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(GameRepository gameRepository,
                      UserRepository userRepository, 
                      SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Game createGame(String username, String gameType, Integer timeControlMinutes, Boolean isRated, String customRules) {
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
            game.setWhitePlayer(user);
            game.setGameType(Optional.ofNullable(gameType).orElse("standard"));
            game.setTimeControlMinutes(Optional.ofNullable(timeControlMinutes).orElse(10));
            game.setStatus(GameStatus.WAITING_FOR_OPPONENT.name());
            game.setCreatedAt(LocalDateTime.now());
            game.setIsPlayerTurn(com.chess.core.Alliance.WHITE);

            // Create game manager with current state
            if(game.getGameType().equals("standard")){
                try {
                    IBoard board = IBoard.createStandardBoard();
                    game.setBoard(board.serialize());
                } catch (Exception e) {
                    logger.error("Error creating standard board: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to create standard board: " + e.getMessage(), e);
                }
            } else {
                logger.warn("Unsupported game type: {}", game.getGameType());
                throw new IllegalArgumentException("Unsupported game type: " + game.getGameType());
            }
            
            // Save and return the game
            return gameRepository.save(game);
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
            if (game.getBlackPlayer() != null) {
                throw new IllegalStateException("Game is already full");
            }

            // Set the black player
            game.setBlackPlayer(joiningUser);
            
            // Update game status to IN_PROGRESS
            game.setStatus(GameStatus.IN_PROGRESS.name());
            
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

    /**
     * Get all active games (IN_PROGRESS) for a specific user by their username
     * @param username The username of the user
     * @return List of active games where the user is either the white or black player
     */
    public List<Game> getActiveGamesByUsername(String username) {
        // Find the user first
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        
        // Use the existing method with the user's ID
        return getActiveGamesForUser(user.getId());
    }

    /**
     * Get all active games (IN_PROGRESS) for a specific user
     * @param userId The ID of the user
     * @return List of active games where the user is either the white or black player
     */
    public List<Game> getActiveGamesForUser(String userId) {
        // Get all games where the user is either the white or black player
        List<Game> userGames = gameRepository.findByWhitePlayerIdOrBlackPlayerId(userId, userId);
        
        // Filter to only include games with IN_PROGRESS status
        return userGames.stream()
            .filter(game -> game.getStatus().equals(GameStatus.IN_PROGRESS.name()))
            .collect(Collectors.toList());
    }

    public List<Game> getLastGamesForUser(String userId) {
        // Get all games where the user is either the white or black player
        List<Game> userGames = gameRepository.findLastGamesByWhitePlayerIdOrBlackPlayerId(userId, userId);
        
        // Sort games by createdAt in descending order (most recent first) and limit to 6
        return userGames;
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
        
        // Get current game state
        IBoard currentBoard = IBoard.deserialize(game.getBoard(), game.getLastMoveData());
        
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

        // Play appropriate sound based on move type
        if(move instanceof CapturingMove) {
            Sounduser.playCaptureSound();
        } else {
            Sounduser.playMoveSound();
        }
            
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///   With each move a new currentBoard is created to represent the new state of the tiles and the turn of the next users.     //           
        ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
        ///   Also Current and Opponent user have changed.                                                                      //
        ///   The new current user gets the opposite color of the previous current user. Same applies for the opponent.       //
        ///   Tiles, current user and opponent user are created with the currentBoard, and are immutable afterwards.                 //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        CurrentPlayer currentPlayer = (CurrentPlayer) newBoard.getCurrentPlayer();

        if (currentPlayer.isCheckmate()) {
            Sounduser.playCheckmateSound();
            game.setStatus(com.chess.model.entity.Game.GameStatus.CHECKMATE.name());
        } else if(currentPlayer.isDraw() == GameStatus.DRAW) {
            Sounduser.playCheckSound();
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
            Sounduser.playCheckSound();
        }
        

        // Store the serialized last move data and check for castling
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode moveData = objectMapper.createObjectNode();
            
            // Add common move properties
            moveData.put("sourceCoordinate", move.getSourceCoordinate());
            moveData.put("targetCoordinate", move.getTargetCoordinate());
            moveData.put("pieceSymbol", move.getPieceToMove().getPieceSymbol().toString());
            moveData.put("pieceAlliance", move.getPieceToMove().getPieceAlliance().toString());
            
            if (move instanceof com.chess.core.moves.noncapturing.PawnJumpMove) {
                moveData.put("moveType", "PAWN_JUMP");
            } else {
                moveData.put("moveType", "NORMAL");
            }
            
            // Store the serialized move data
            game.setLastMoveData(objectMapper.writeValueAsString(moveData));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize move data", e);
        }

        // Store the serialized new board        
        game.setBoard(newBoard.serialize());

        // Increment move count each time a move is made
        game.setMoveCount(game.getMoveCount() + 1);
        game.setIsPlayerTurn(newBoard.getCurrentPlayer().getAlliance());
        
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
}
