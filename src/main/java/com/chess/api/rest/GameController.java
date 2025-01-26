package com.chess.api.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.api.dto.request.MoveRequest;
import com.chess.application.game.GameService;
import com.chess.core.Game;
import org.springframework.http.MediaType;
@RestController
@RequestMapping("/api/chess")
public class GameController {
    private final GameService gameService = new GameService();

   @PostMapping("/create")
public ResponseEntity<Map<String, String>> createGame() {
    try {
        Map<String, Game> gameMap = gameService.createNewGame();
        String gameId = gameMap.keySet().iterator().next();
        System.out.println("Game ID: " + gameId);
        gameService.addStandardGame(gameId);
        
        Map<String, String> response = new HashMap<>();
        response.put("gameId", gameId);
        
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    } catch (Exception e) {
        System.err.println("Error creating game: " + e.getMessage());
        e.printStackTrace();
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error creating game");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(errorResponse);
    }
}

@GetMapping("/{gameId}")
public ResponseEntity<Game> getGame(@PathVariable String gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    return ResponseEntity.ok(game); 
}

    @PostMapping("/{gameId}/move")
    public ResponseEntity<?> makeMove(@PathVariable String gameId, @RequestBody MoveRequest moveRequest) {  
        try {
            Game game = gameService.makeMove(gameId, moveRequest);
            if (game == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
            }
            return ResponseEntity.ok(game); // Return the updated game state
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }    
    }
}
