package com.chess.application.game;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.chess.api.dto.request.MoveRequest;
import com.chess.core.Game;
import com.chess.core.board.Board;
import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;

import jakarta.servlet.http.HttpServletRequest;


@Service
public class GameService {

    private static Map<String, Game> activeGames = new ConcurrentHashMap<>();
    public static final ThreadLocal<String> currentGameId = new ThreadLocal<>();
    
    public static String getCurrentGameId(){
        return currentGameId.get();
    }

    public Map<String, Game> createNewGame(HttpServletRequest request) {
        String newGameId = UUID.randomUUID().toString();
        request.getSession().setAttribute("gameId", newGameId); // Store gameId in session
        currentGameId.set(newGameId);
        Game newGame = new Game();
        activeGames.put(newGameId, newGame);
        addStandardGame(request);
        return activeGames;
    }

    public Map<String, Game> addStandardGame(HttpServletRequest request) {
        try {
            IBoard board = Board.createStandardBoard();
            activeGames.get(( String) request.getSession().getAttribute("gameId")).setBoard(board);
            return activeGames;
        } catch (Exception e) {
            System.err.println("Error adding standard game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Game> createNewTestGame(HttpServletRequest request) {
        Game newGame = new Game();
        activeGames.put((String) request.getSession().getAttribute("gameId"), newGame);
        return activeGames;
    }

    public Map<String, Game> addTestGame(IBoard board, String gameId) {
        activeGames.get(gameId).setBoard(board);
        return activeGames;
    }

    public Game getGame(HttpServletRequest request) {
        return activeGames.get((String) request.getSession().getAttribute("gameId"));
    }

    public static Game getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public void deleteGame(String gameId) {
        activeGames.remove(gameId);
    }

    public Game makeMove(HttpServletRequest sessionRequest, MoveRequest moverequest){
        Game game = getGame(sessionRequest);
        currentGameId.set((String) sessionRequest.getSession().getAttribute("gameId"));

        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        
        // Find matching legal move from current position
        Move move = game.getBoard()
        .getCurrentPlayer()
        .getMoves()
        .stream()
        .filter(m -> m.getSourceCoordinate() == moverequest.getSourceCoordinate() 
                && m.getTargetCoordinate() == moverequest.getTargetCoordinate())
        .findFirst()
        .orElse(null);

        if (move != null && activeGames.containsKey((String) sessionRequest.getSession().getAttribute("gameId"))) {
             return activeGames.get((String) sessionRequest.getSession().getAttribute("gameId")).updateGame(move);
        } else {
            return null;
        }
        // the error is i am reading from activegames instead of the current game
    }
}