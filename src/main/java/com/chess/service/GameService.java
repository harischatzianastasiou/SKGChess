package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.core.GameManager;
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
        Game game = new Game();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        game.setWhitePlayer(user);
        game.setFenPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        game.setGameType(Optional.ofNullable(gameType).orElse("standard"));
        game.setTimeControlMinutes(Optional.ofNullable(timeControlMinutes).orElse(10));
        game.setIsRated(Optional.ofNullable(isRated).orElse(true));
        game.setCustomRules(Optional.ofNullable(customRules).orElse(""));
        game.setStatus(GameStatus.WAITING_FOR_OPPONENT);
        game.setCreatedAt(LocalDateTime.now());
        game.setIsPlayerTurn(com.chess.core.Alliance.WHITE);
        return gameRepository.save(game);
    }

    @Transactional
    public IBoard joinGame(String gameId, String username) {
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
        Game savedGame = gameRepository.save(game);

        // Get current game state
        String currentFen = game.getFenPosition();
        String lastMovePgn = game.getLastMovePgn();
        int moveCount = game.getMoveCount();
        boolean isWhitePlayerCastled = game.isWhitePlayerCastled();
        boolean isBlackPlayerCastled = game.isBlackPlayerCastled();
        
        // Create game manager with current state
        GameManager gameManager = new GameManager(currentFen, lastMovePgn, game.getIsPlayerTurn() == com.chess.core.Alliance.WHITE ? isWhitePlayerCastled : isBlackPlayerCastled, moveCount);
        IBoard board = gameManager.getBoard();
        
        // Send WebSocket notification to the white player that the game has started
        logger.info("Sending game started notification for game ID: {}", gameId);
        
        
        return board;
    }

    public Game getGameById(String gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional
    public IBoard makeMove(String gameId, int sourceCoordinate, int targetCoordinate) {
        // Check if game exists
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        
        // Get current game state
        String currentFen = game.getFenPosition();
        String lastMovePgn = game.getLastMovePgn();
        int moveCount = game.getMoveCount();
        boolean isWhitePlayerCastled = game.isWhitePlayerCastled();
        boolean isBlackPlayerCastled = game.isBlackPlayerCastled();
        
        // Create game manager with current state
        GameManager gameManager = new GameManager(currentFen, lastMovePgn, game.getIsPlayerTurn() == com.chess.core.Alliance.WHITE ? isWhitePlayerCastled : isBlackPlayerCastled, moveCount);
        
        // Find the move from legal moves
        com.chess.core.moves.Move move = gameManager.getMoves()
            .stream()
            .filter(m -> m.getSourceCoordinate() == sourceCoordinate 
                    && m.getTargetCoordinate() == targetCoordinate)
            .findFirst()
            .orElseThrow(() -> new InvalidMoveException("Invalid move"));
        
        // Execute the move
        com.chess.core.board.IBoard newBoard = gameManager.executeMove(move);
        
        // Create a new position record
        Position newPosition = new Position();
        newPosition.setFen(newBoard.getFEN());
        newPosition.setMoveNumber(moveCount + 1);
        newPosition.setNextPlayerTurn(newBoard.getCurrentPlayer().getAlliance());
        newPosition.setWhiteCastled(game.isWhitePlayerCastled());
        newPosition.setBlackCastled(game.isBlackPlayerCastled());
        newPosition.setLastMovePgn(move.toString());
        newPosition.setGame(game);
        
        // Update game state
        game.setFenPosition(newBoard.getFEN());
        game.setMoveCount(moveCount + 1);
        game.setLastMovePgn(move.toString());
        game.setIsPlayerTurn(newBoard.getCurrentPlayer().getAlliance());
        
        // Check for castling
        if (move.toString().equals("O-O") || move.toString().equals("O-O-O")) {
            if (game.getIsPlayerTurn() == com.chess.core.Alliance.WHITE) {
                game.setWhitePlayerCastled(true);
            } else {
                game.setBlackPlayerCastled(true);
            }
        }
        
        // Update game status based on the new board state
        if (gameManager.isCheckmate()) {
            game.setStatus(Game.GameStatus.CHECKMATE);
        } else if (gameManager.getGameStatus() == GameManager.GameStatus.DRAW) {
            game.setStatus(Game.GameStatus.DRAW);
        } else {
            game.setStatus(Game.GameStatus.IN_PROGRESS);
        }
        
        // Add the new position to the game
        game.getPositions().add(newPosition);
        
        // Save the game (which will cascade to save the position)
        game = gameRepository.save(game);

        return newBoard;
    }
}
