package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.core.Alliance;
import com.chess.core.board.IBoard;
import com.chess.exception.GameAlreadyJoinedException;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.GameNotWaitingForOpponentException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.model.entity.Position;
import com.chess.model.entity.User;
import com.chess.repository.GameRepository;
import com.chess.repository.PositionRepository;
import com.chess.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.chess.core.player.Player;
import com.chess.core.moves.Move;
import com.chess.core.moves.capturing.CapturingMove;
import com.chess.core.player.CurrentPlayer;
import com.chess.core.board.IBoard;
import com.chess.util.Sounduser;
@Service
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(GameRepository gameRepository, 
                      PositionRepository positionRepository,
                      UserRepository userRepository, 
                      SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.positionRepository = positionRepository;
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
            
            // Find user
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
            
            // Create game object
            Game game = new Game();
            game.setWhitePlayer(user);
            game.setGameType(Optional.ofNullable(gameType).orElse("standard"));
            game.setTimeControlMinutes(Optional.ofNullable(timeControlMinutes).orElse(10));
            game.setIsRated(Optional.ofNullable(isRated).orElse(true));
            game.setCustomRules(Optional.ofNullable(customRules).orElse(""));
            game.setStatus(GameStatus.WAITING_FOR_OPPONENT);
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
        } catch (UserNotFoundException e) {
            // Re-throw user not found exception
            throw e;
        } catch (Exception e) {
            // Log and wrap other exceptions
            logger.error("Error creating game: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create game: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Game joinGame(String gameId, String username) {
        //Check if user exists
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));

        //Check if game exists
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        //Check if game is waiting for opponent
        if(game.getStatus() != GameStatus.WAITING_FOR_OPPONENT){
            throw new GameNotWaitingForOpponentException(gameId);
        }

        //Check if user is already in the game
        if(game.getBlackPlayer() != null){
            throw new GameAlreadyJoinedException(gameId);
        }

        //Set black player and new status of game
        game.setBlackPlayer(user);
        game.setStatus(GameStatus.IN_PROGRESS);

        //Save game
        return gameRepository.save(game);
    }

    public Game getGameById(String gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional
    public Game makeMove(String gameId, int sourceCoordinate, int targetCoordinate) {
        // Check if game exists
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        // Get current game state
        IBoard currentBoard = IBoard.deserialize(game.getBoard(), game.getLastMoveData(), game.getIsPlayerTurn() == com.chess.core.Alliance.WHITE ? game.isWhitePlayerCastled() : game.isBlackPlayerCastled());
        
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
        } else if(currentPlayer.isInCheck()) {
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
            
            if (move instanceof com.chess.core.moves.noncapturing.KingSideCastleMove) {
                if (game.getIsPlayerTurn() == com.chess.core.Alliance.WHITE) {
                    moveData.put("moveType", "KING_SIDE_CASTLE");
                    game.setWhitePlayerCastled(true);
                } else if (game.getIsPlayerTurn() == com.chess.core.Alliance.BLACK) {
                    moveData.put("moveType", "KING_SIDE_CASTLE");
                    game.setBlackPlayerCastled(true);
                }
            } else if (move instanceof com.chess.core.moves.noncapturing.QueenSideCastleMove) {
                if (game.getIsPlayerTurn() == com.chess.core.Alliance.WHITE) {
                    moveData.put("moveType", "QUEEN_SIDE_CASTLE");
                    game.setWhitePlayerCastled(true);
                } else if (game.getIsPlayerTurn() == com.chess.core.Alliance.BLACK) {
                    moveData.put("moveType", "QUEEN_SIDE_CASTLE");
                    game.setBlackPlayerCastled(true);
                }
            } else if (move instanceof com.chess.core.moves.noncapturing.PawnJumpMove) {
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

        // Create a new position record
        Position newPosition = new Position();
        newPosition.setFen(newBoard.getFEN());
        // newPosition.setMoveNumber(moveCount + 1);
        newPosition.setNextPlayerTurn(newBoard.getCurrentPlayer().getAlliance());
        newPosition.setGame(game);
        
        // Update game state
        game.setFenPosition(newBoard.getFEN());
        // game.setMoveCount(moveCount + 1);
        game.setIsPlayerTurn(newBoard.getCurrentPlayer().getAlliance());
        
        // Add the new position to the game
        game.getPositions().add(newPosition);
        
        // Save the game (which will cascade to save the position)
        return gameRepository.save(game);
    } 
}
