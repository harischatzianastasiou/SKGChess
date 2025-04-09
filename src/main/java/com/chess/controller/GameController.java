package com.chess.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chess.dto.GameDTO;
import com.chess.dto.websocket.MoveDTO;
import com.chess.model.entity.Game;
import com.chess.service.GameService;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/game/create/{userId}")
    @ResponseBody
    public GameDTO createGame(@PathVariable String userId) {
        Game game = gameService.createGame(userId);
        return GameDTO.fromGame(game);
    }

    @PostMapping("/game/join/{userId}")
    @ResponseBody
    public GameDTO joinGame(@PathVariable String userId) {
        // Find the oldest game with WAITING_FOR_OPPONENT status
        String gameId = gameService.findOldestWaitingGameId();
        
        // If no waiting game found, return null
        if (gameId == null) {
            return null;
        }
        
        // Join the game
        Game game = gameService.joinGame(gameId, userId);
        return GameDTO.fromGame(game);
    }

    @GetMapping("/game/{gameId}")
    public String getGame(@PathVariable String gameId, Model model) {//else return game object in json format
        Game game = gameService.getGameById(gameId);
        model.addAttribute("game", game);
        return "game";  // This tells Spring to use game.html template
    }

    @MessageMapping("/game/{gameId}/move")
    @SendTo("/topic/game/{gameId}")
    public MoveDTO handleMove(MoveDTO moveDTO, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        // String userId = sessionManager.getUserIdFromSession(sessionId);
        
        // if (sessionManager.isSessionActive(sessionId) && 
        //     userId != null && 
        //     userId.equals(moveDTO.getUserId())) {
            
            Game game = gameService.updateGame(moveDTO);
            if (game != null) {
                return moveDTO;
            }
        
        return null;
    }

    // @MessageMapping("/game/{gameId}/chat")
    // @SendTo("/topic/game/{gameId}")
    // public ChatDTO handleChat(ChatDTO chatDTO, SimpMessageHeaderAccessor headerAccessor) {
    //     String sessionId = headerAccessor.getSessionId();
    //     String userId = sessionManager.getUserIdFromSession(sessionId);
        
    //     if (sessionManager.isSessionActive(sessionId) && userId != null) {
    //         chatDTO.setTimestamp(System.currentTimeMillis());
    //         chatDTO.setSender(userId);
    //         return chatDTO;
    //     }
    //     return null;
    // }
}
