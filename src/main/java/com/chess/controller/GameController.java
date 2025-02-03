package com.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.chess.model.entity.Game;
import com.chess.service.GameService;
import com.chess.dto.request.MoveRequest;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/game")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/create")
    public void createMatchmakingGame(String player1Id, String player2Id) {
        gameService.createGame(player1Id, player2Id);
    }

    @GetMapping("/{gameId}")
    public String getGame(@PathVariable String gameId, Model model) {
        Game game = gameService.getGameById(gameId);
        model.addAttribute("game", game);
        return "game";
    }

    @PostMapping("/{gameId}/move")
    @ResponseBody
    public ResponseEntity<?> makeMove(@PathVariable String gameId, @RequestBody MoveRequest moveRequest) {
        try {
            Game game = gameService.updateGame(moveRequest);
            
            if (game != null) {
                JsonObject gameState = new JsonObject();
                gameState.addProperty("type", "GAME_UPDATE");
                gameState.addProperty("gameId", game.getId().toString());
                gameState.addProperty("fenPosition", game.getFenPosition());
                gameState.addProperty("status", game.getStatus().toString());
                gameState.addProperty("pgnMoves", game.getPgnMoves());
                
                messagingTemplate.convertAndSend("/topic/game/" + game.getId(), gameState.toString());
                return ResponseEntity.ok(game);
            } else {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("type", "MOVE_ERROR");
                errorMessage.addProperty("message", "Invalid move");
                messagingTemplate.convertAndSendToUser(
                    moveRequest.getPlayerId(), 
                    "/queue/errors", 
                    errorMessage.toString()
                );
                return ResponseEntity.badRequest().body(errorMessage.toString());
            }
        } catch (Exception e) {
            JsonObject errorMessage = new JsonObject();
            errorMessage.addProperty("type", "ERROR");
            errorMessage.addProperty("message", "An error occurred: " + e.getMessage());
            messagingTemplate.convertAndSendToUser(
                moveRequest.getPlayerId(), 
                "/queue/errors", 
                errorMessage.toString()
            );
            return ResponseEntity.badRequest().body(errorMessage.toString());
        }
    }
}
