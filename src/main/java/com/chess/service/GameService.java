package com.chess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.core.GameManager;
import com.chess.core.moves.Move;
import com.chess.dto.websocket.MoveDTO;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.model.entity.Game;
import com.chess.model.entity.User;
import com.chess.model.session.SessionManager;
import com.chess.repository.GameRepository;
import com.chess.repository.UserRepository;
import com.google.gson.JsonObject;

@Service
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(GameRepository gameRepository, 
                      UserRepository userRepository, 
                      SimpMessagingTemplate messagingTemplate,
                      SessionManager sessionManager) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Game createGame(String WhiteUserId, String BlackUserId) {
        User WhiteUser = userRepository.findById(WhiteUserId)
            .orElseThrow(() -> new UserNotFoundException(WhiteUserId));
        User BlackUser = userRepository.findById(BlackUserId)
            .orElseThrow(() -> new UserNotFoundException(BlackUserId));
            
        Game game = new Game(WhiteUser, BlackUser);
        return gameRepository.save(game);
    }

    @Transactional
    public Game updateGame(MoveDTO moveRequest) {
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
}