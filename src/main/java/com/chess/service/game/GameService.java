package com.chess.service.game;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chess.dto.request.MoveRequest;
import com.chess.core.Game;
import com.chess.core.board.Board;
import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.google.gson.JsonObject;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class GameService {

    // Store waiting players in a simple queue
    private static Queue<String> waitingPlayers = new ConcurrentLinkedQueue<>();
    
    // Store active games
    private static Map<String, Game> activeGames = new ConcurrentHashMap<>();
    
    // Store player to game mappings
    private static Map<String, String> playerGameMap = new ConcurrentHashMap<>();

    public static final ThreadLocal<String> currentGameId = new ThreadLocal<>();
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public static String getCurrentGameId() {
        return currentGameId.get();
    }

    // Create game for REST endpoint
    public Map<String, Game> createNewGame(HttpServletRequest request) {
        String newGameId = UUID.randomUUID().toString();
        request.getSession().setAttribute("gameId", newGameId);
        currentGameId.set(newGameId);
        Game newGame = new Game();
        activeGames.put(newGameId, newGame);
        addStandardGame(request);
        return activeGames;
    }

    // Create game for WebSocket matchmaking
    public Game createMatchmakingGame(String player1SessionId, String player2SessionId) {
        String gameId = UUID.randomUUID().toString();
        Game newGame = new Game();
        IBoard board = Board.createStandardBoard();
        newGame.setBoard(board);
        
        // Store the game
        activeGames.put(gameId, newGame);
        
        // Map players to this game
        playerGameMap.put(player1SessionId, gameId);
        playerGameMap.put(player2SessionId, gameId);
        
        // Notify players
        notifyGameCreated(player1SessionId, player2SessionId, gameId);
        
        return newGame;
    }

    private void notifyGameCreated(String player1Id, String player2Id, String gameId) {
        // Notify player 1 (White)
        JsonObject player1Message = new JsonObject();
        player1Message.addProperty("type", "GAME_CREATED");
        player1Message.addProperty("gameId", gameId);
        player1Message.addProperty("color", "WHITE");
        player1Message.addProperty("opponent", player2Id);
        messagingTemplate.convertAndSendToUser(player1Id, "/queue/game", player1Message.toString());

        // Notify player 2 (Black)
        JsonObject player2Message = new JsonObject();
        player2Message.addProperty("type", "GAME_CREATED");
        player2Message.addProperty("gameId", gameId);
        player2Message.addProperty("color", "BLACK");
        player2Message.addProperty("opponent", player1Id);
        messagingTemplate.convertAndSendToUser(player2Id, "/queue/game", player2Message.toString());
    }

    public void addPlayerToQueue(String sessionId) {
        waitingPlayers.offer(sessionId);
        tryMatchPlayers();
    }

    public void removePlayerFromQueue(String sessionId) {
        waitingPlayers.remove(sessionId);
    }

    private void tryMatchPlayers() {
        if (waitingPlayers.size() >= 2) {
            String player1 = waitingPlayers.poll();
            String player2 = waitingPlayers.poll();
            if (player1 != null && player2 != null) {
                createMatchmakingGame(player1, player2);
            }
        }
    }

    public Game getGameByPlayer(String sessionId) {
        String gameId = playerGameMap.get(sessionId);
        return gameId != null ? activeGames.get(gameId) : null;
    }

    public Map<String, Game> addStandardGame(HttpServletRequest request) {
        try {
            IBoard board = Board.createStandardBoard();
            activeGames.get((String) request.getSession().getAttribute("gameId")).setBoard(board);
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
        // Remove game and player mappings
        activeGames.remove(gameId);
        playerGameMap.entrySet().removeIf(entry -> entry.getValue().equals(gameId));
    }

    public Game makeMove(HttpServletRequest sessionRequest, MoveRequest moverequest) {
        Game game = getGame(sessionRequest);
        currentGameId.set((String) sessionRequest.getSession().getAttribute("gameId"));

        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        
        // Find matching legal move from current position
        Move move = game.getMoves()
            .stream()
            .filter(m -> m.getSourceCoordinate() == moverequest.getSourceCoordinate() 
                    && m.getTargetCoordinate() == moverequest.getTargetCoordinate())
            .findFirst()
            .orElse(null);

        if (move != null && activeGames.containsKey((String) sessionRequest.getSession().getAttribute("gameId"))) {
            Game updatedGame = game.updateGame(move);
            
            // Notify both players about the move via WebSocket
            String gameId = (String) sessionRequest.getSession().getAttribute("gameId");
            notifyGameUpdate(gameId, updatedGame);
            
            return updatedGame;
        }
        return null;
    }

    public void handleWebSocketMove(String sessionId, int sourceCoordinate, int targetCoordinate) {
        // Get the game for this player
        Game game = getGameByPlayer(sessionId);
        if (game == null) {
            System.err.println("Game not found for player: " + sessionId);
            return;
        }

        // Create move request
        MoveRequest moveRequest = new MoveRequest(sourceCoordinate, targetCoordinate);

        // Find matching legal move
        Move move = game.getMoves()
            .stream()
            .filter(m -> m.getSourceCoordinate() == moveRequest.getSourceCoordinate() 
                    && m.getTargetCoordinate() == moveRequest.getTargetCoordinate())
            .findFirst()
            .orElse(null);

        if (move != null) {
            Game updatedGame = game.updateGame(move);
            
            // Get the gameId for this player
            String gameId = playerGameMap.get(sessionId);
            
            // Update the game in our active games map
            activeGames.put(gameId, updatedGame);
            
            // Notify both players about the move
            notifyGameUpdate(gameId, updatedGame);
        } else {
            // Notify player about invalid move
            JsonObject errorMessage = new JsonObject();
            errorMessage.addProperty("type", "MOVE_ERROR");
            errorMessage.addProperty("message", "Invalid move");
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/game", errorMessage.toString());
        }
    }

    private void notifyGameUpdate(String gameId, Game game) {
        JsonObject gameState = new JsonObject();
        gameState.addProperty("type", "GAME_UPDATE");
        gameState.addProperty("gameId", gameId);
        gameState.addProperty("currentPlayer", game.getBoard().getCurrentPlayer().getAlliance().toString());
        
        if (game.getGameStatus().toString().equals("CHECKMATE")) {
            gameState.addProperty("status", "CHECKMATE");
            gameState.addProperty("winner", game.getBoard().getOpponentPlayer().getAlliance().toString());
        } else if (game.getGameStatus().toString().equals("STALEMATE")) {
            gameState.addProperty("status", "DRAW");
            gameState.addProperty("drawType", "STALEMATE");
        } else {
            gameState.addProperty("status", "ACTIVE");
        }

        // Send update to both players
        playerGameMap.entrySet().stream()
            .filter(entry -> entry.getValue().equals(gameId))
            .forEach(entry -> {
                messagingTemplate.convertAndSendToUser(
                    entry.getKey(), 
                    "/queue/game", 
                    gameState.toString()
                );
            });
    }
}