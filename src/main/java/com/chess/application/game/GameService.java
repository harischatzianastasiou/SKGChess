package com.chess.application.game;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.chess.api.dto.request.MoveRequest;
import com.chess.core.Game;
import com.chess.core.moves.Move;


@Service
public class GameService {
    private Map<String, Game> activeGames = new ConcurrentHashMap<>();

    public GameService() {
    }

    public String createNewGame() {
        Map<String, Game> gameMap = Game.createNewGame();
        activeGames.putAll(gameMap);
        return gameMap.keySet().iterator().next();
    }

    public void addStandardGame(String gameId) {
        Game.addStandardGame(gameId);
        return;
    }

    public String getGameId(String gameId) {
        if(activeGames.containsKey(gameId)) {
            return gameId;
        }
        return null;
    }

    public Game getGame(String gameId) {
        if(activeGames.containsKey(gameId)) {
            return activeGames.get(gameId);
        }
        return null;
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