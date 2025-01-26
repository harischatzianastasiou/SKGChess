package com.chess.application.game;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.chess.api.dto.request.MoveRequest;
import com.chess.core.Game;
import com.chess.core.moves.Move;
import com.chess.core.board.IBoard;
import com.chess.core.board.Board;
import java.util.Collection;

@Service
public class GameService {
    private static Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private static ThreadLocal<String> currentGameId = new ThreadLocal<>();

    public static void setCurrentGameId(String gameId) {
        currentGameId.set(gameId);
    }

    public static Game getCurrentGame() {
        String gameId = currentGameId.get();
        if (gameId != null) {
            return activeGames.get(gameId);
        }
        throw new IllegalStateException("No current game set for this thread");
    }

    public static String getCurrentGameId() {
        return currentGameId.get();
    }

    public Move getLastMove() {
        return getCurrentGame().getLastMove();
    }

    public static Map<String, Game> createNewGame() {
        String newGameId = UUID.randomUUID().toString();
        Game newGame = new Game(newGameId);
        activeGames.put(newGameId, newGame);
        return activeGames;
    }

    public Map<String, Game> addStandardGame(String gameId) {
        try {
            IBoard board = Board.createStandardBoard(gameId);
            activeGames.get(gameId).setBoard(board);
            return activeGames;
        } catch (Exception e) {
            System.err.println("Error adding standard game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Game> createNewTestGame(String gameId) {
        Game newGame = new Game(gameId);
        activeGames.put(gameId, newGame);
        return activeGames;
    }

    public Map<String, Game> addTestGame(IBoard board, String gameId) {
        activeGames.get(gameId).setBoard(board);
        return activeGames;
    }

    public static Game getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public Collection<Move> getMoveHistory(String gameId) {
        return activeGames.get(gameId).getMoveHistory();
    }

    public Map<String, Game> getActiveGames() {
        return activeGames;
    }

    public void deleteGame(String gameId) {
        activeGames.remove(gameId);
    }

    public Game makeMove(String gameId, MoveRequest request){
        Game game = getGame(gameId);

        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        
        // Find matching legal move from current position
        Move move = game.getBoard()
        .getCurrentPlayer()
        .getMoves()
        .stream()
        .filter(m -> m.getSourceCoordinate() == request.getSourceCoordinate() 
                && m.getTargetCoordinate() == request.getTargetCoordinate())
        .findFirst()
        .orElse(null);

        if (move != null && activeGames.containsKey(gameId)) {
             return activeGames.get(gameId).updateGame(move);
        } else {
            return null;
        }
    }
}