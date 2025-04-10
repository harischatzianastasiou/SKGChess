package com.chess.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.chess.dto.websocket.MoveDTO;
import com.chess.model.entity.Game;
import com.chess.service.GameService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MoveWebSocketController {
    
    private final GameService gameService;
    
    @MessageMapping("{gameId}/move")
    @SendTo("/topic/game/{gameId}")
    public MoveDTO handleMove(MoveDTO moveDTO, SimpMessageHeaderAccessor headerAccessor) {
            
            Game game = gameService.updateGame(moveDTO);
            if (game != null) {
                return moveDTO;
            }
        
        return null;
    }
}
