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

import com.chess.dto.websocket.ChatDTO;
import com.chess.dto.websocket.MoveDTO;
import com.chess.model.entity.Game;
import com.chess.model.session.SessionManager;
import com.chess.service.GameService;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate, SessionManager sessionManager) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
    }

    @MessageMapping("/game/create/")
    @SendTo("/topic/game/{userId1}/{userId2}")  // Only players with this specific topic will receive it
    public Game createGame(@PathVariable String userId1, @PathVariable String userId2) {
        return gameService.createGame(userId1,userId2);
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
        String userId = sessionManager.getUserIdFromSession(sessionId);
        
        if (sessionManager.isSessionActive(sessionId) && 
            userId != null && 
            userId.equals(moveDTO.getUserId())) {
            
            Game game = gameService.updateGame(moveDTO);
            if (game != null) {
                return moveDTO;
            }
        }
        return null;
    }

    @MessageMapping("/game/{gameId}/chat")
    @SendTo("/topic/game/{gameId}")
    public ChatDTO handleChat(ChatDTO chatDTO, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = sessionManager.getUserIdFromSession(sessionId);
        
        if (sessionManager.isSessionActive(sessionId) && userId != null) {
            chatDTO.setTimestamp(System.currentTimeMillis());
            chatDTO.setSender(userId);
            return chatDTO;
        }
        return null;
    }
}
