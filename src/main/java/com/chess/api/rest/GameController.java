package com.chess.api.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.api.dto.request.MoveRequest;
import com.chess.application.game.GameService;
import com.chess.core.Game;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/chess")
public class GameController {
    private final GameService gameService = new GameService();

   @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createGame(HttpServletRequest request) {
        try {
            gameService.createNewGame(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Game created successfully");
            
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        } catch (Exception e) {
            System.err.println("Error creating game: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating game");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(errorResponse);
        }
    }

    @GetMapping("/read")
    public ResponseEntity<Game> getGame(HttpServletRequest request) {
        // Retrieve the current game
        Game game = gameService.getGame(request);
        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(game);
    }

    @PostMapping("/move")
    public ResponseEntity<?> makeMove(HttpServletRequest sessionRequest, @RequestBody MoveRequest moveRequest) {  
        try {
            Game game = gameService.makeMove(sessionRequest, moveRequest);
            if (game == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
            }
            return ResponseEntity.ok(game); // Return the updated game state
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }    
    }
}
