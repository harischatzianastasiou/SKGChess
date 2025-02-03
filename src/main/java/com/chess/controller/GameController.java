package com.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chess.dto.request.MoveRequest;
import com.chess.model.entity.Game;
import com.chess.service.GameService;
import com.google.gson.JsonObject;

@Controller
@RequestMapping("/game")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

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
