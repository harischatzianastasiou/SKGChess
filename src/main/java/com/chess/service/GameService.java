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
import com.chess.core.moves.Move;
import com.chess.dto.websocket.MoveDTO;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.GameNotWaitingForOpponentException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.exception.GameAlreadyJoinedException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.model.entity.User;
import com.chess.repository.GameRepository;
import com.chess.repository.UserRepository;

@Service
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(GameRepository gameRepository, 
                      UserRepository userRepository, 
                      SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
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
        return gameRepository.save(game);
    }

    @Transactional
    public Game joinGame(String gameId, String UserId) {
        //Check if user exists
        User user = userRepository.findById(UserId)
            .orElseThrow(() -> new UserNotFoundException(UserId));

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

    @Transactional
    public Game updateGame(MoveDTO moveRequest) {
        //Check if game exists
        Game game = gameRepository.findById(moveRequest.getGameId())
            .orElseThrow(() -> new GameNotFoundException(moveRequest.getGameId()));
            
        String boardFen = game.getFenPosition();
        int moveCount = game.getMoveCount();
        String pgnMoves = game.getPgnMoves();
        boolean hascurrentPlayerCastled = false;

        if(!game.isBlackPlayerCastled() && !game.isWhitePlayerCastled()){
            // Determine if current user has castled by analyzing PGN moves
            if (pgnMoves != null && !pgnMoves.isEmpty()) {
                // Get current user's color from FEN by finding the turn indicator after the board position
                String[] fenParts = boardFen.split(" ");
                boolean isWhiteTurn = fenParts.length > 1 && fenParts[1].equals("w");

                if(
                    (isWhiteTurn && !game.isWhitePlayerCastled())
                    ||
                    (!isWhiteTurn && !game.isBlackPlayerCastled())
                ){
                    // Split PGN moves and check for castling
                    String[] moves = pgnMoves.split("\\s+");
                    for (int i = 0; i < moves.length; i++) {
                        String move = moves[i];
                        // Skip move numbers (e.g., "1.", "2.")
                        if (move.contains(".")) continue;
                        
                        // Check if this move was made by the current user
                        boolean isMoveBycurrentPlayer = (i % 2 == 0) == isWhiteTurn;
                        if (isMoveBycurrentPlayer && (move.equals("O-O") || move.equals("O-O-O"))) {
                            hascurrentPlayerCastled = true;
                            if (isWhiteTurn) {
                                game.setWhitePlayerCastled(true);
                            } else {
                                game.setBlackPlayerCastled(true);
                            }
                            break;
                        }
                    }
                }
            }
        }

        GameManager gameManager = new GameManager(boardFen, moveCount, pgnMoves, hascurrentPlayerCastled);

        Move move = gameManager.getMoves()
                    .stream()
                    .filter(m -> m.getSourceCoordinate() == moveRequest.getMove().getSourceCoordinate() 
                            && m.getTargetCoordinate() == moveRequest.getMove().getTargetCoordinate())
                    .findFirst()
                    .orElseThrow(() -> new InvalidMoveException("Invalid move"));

        // game.setPgnMoves(gameManager.getPgnMoves());
        // game.setFenPosition(gameManager.getFenPosition());
        // game.setMoveCount(gameManager.getMoveCount());
        // game.setLastMovePgn(gameManager.getLastMovePgn());
        // game.setStatus(gameManager.getGameStatus());
        // game.setDrawType(gameManager.getDrawType());
        // game.setWinner(gameManager.getWinner());
        // game.setEndTime(gameManager.getEndTime());
        // game.setLastMoveTime(gameManager.getLastMoveTime());

        game = gameRepository.save(game);
        return game;
    }

    public Game getGameById(String gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public String getFenPositionByGameId(String gameId) {
        Game game = getGameById(gameId);
        return game.getFenPosition();
    }

    public String getPgnMovesByGameId(String gameId) {
        Game game = getGameById(gameId);
        return game.getPgnMoves();
    }

    public int getMoveCountByGameId(String gameId) {
        Game game = getGameById(gameId);
        return game.getMoveCount();
    }

    public String getLastMovePgnByGameId(String gameId) {
        Game game = getGameById(gameId);
        return game.getLastMovePgn();
    }

    /**
     * Find the oldest game with WAITING_FOR_OPPONENT status
     * @return The game ID of the oldest waiting game, or null if none found
     */
    public String findOldestWaitingGameId() {
        return gameRepository.findOldestWaitingGame()
                .map(Game::getId)
                .orElse(null);
    }
}
