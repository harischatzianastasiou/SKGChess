package com.chess.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.chess.dto.websocket.ChatDTO;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/chat/{gameId}")
    public void handleChat(@DestinationVariable String gameId, ChatDTO chatMessage) {
        // Add server timestamp if not provided
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(System.currentTimeMillis());
        }
        
        // Broadcast the message to all subscribers of this game's chat
        messagingTemplate.convertAndSend("/topic/chat/" + gameId, chatMessage);
    }
} 