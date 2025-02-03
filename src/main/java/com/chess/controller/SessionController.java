package com.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chess.model.session.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
public class SessionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/session/queue/add")
    public void addPlayerToMatchmakingQueue(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String username = payload.get("username").asText();
        String sessionId = headerAccessor.getSessionId();
        logger.info("Adding player to matchmaking queue - username: {}, sessionId: {}", username, sessionId);

        try {
            sessionManager.addToMatchmakingQueue(sessionId, username);
            
            // Send confirmation to the user
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "QUEUE_JOIN_SUCCESS");
            confirmation.addProperty("message", "Successfully joined matchmaking queue");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/game",
                confirmation.toString()
            );

            // Log queue size
            logger.info("Current queue size: {}", sessionManager.getQueueSize());
        } catch (Exception e) {
            logger.error("Error adding player to queue", e);
            
            // Send error to the user
            JsonObject error = new JsonObject();
            error.addProperty("type", "ERROR");
            error.addProperty("message", "Failed to join matchmaking queue");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                error.toString()
            );
        }
    }

    @MessageMapping("/session/queue/remove")
    public void removePlayerFromMatchmakingQueue(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.info("Removing player from matchmaking queue - sessionId: {}", sessionId);
        
        try {
            sessionManager.removeFromMatchmakingQueue(sessionId);
            
            // Send confirmation to the user
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "QUEUE_LEAVE_SUCCESS");
            confirmation.addProperty("message", "Successfully left matchmaking queue");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/game",
                confirmation.toString()
            );
        } catch (Exception e) {
            logger.error("Error removing player from queue", e);
            
            // Send error to the user
            JsonObject error = new JsonObject();
            error.addProperty("type", "ERROR");
            error.addProperty("message", "Failed to leave matchmaking queue");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                error.toString()
            );
        }
    }

    @MessageMapping("/session/player/add")
    public void addPlayerToSession(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String playerId = payload.get("playerId").asText();
        logger.info("Adding player to session - playerId: {}, sessionId: {}", playerId, sessionId);
        
        try {
            sessionManager.addPlayerToSession(sessionId, playerId);
            
            // Send confirmation to the user
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "SESSION_JOIN_SUCCESS");
            confirmation.addProperty("message", "Successfully joined session");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/game",
                confirmation.toString()
            );
        } catch (Exception e) {
            logger.error("Error adding player to session", e);
            
            // Send error to the user
            JsonObject error = new JsonObject();
            error.addProperty("type", "ERROR");
            error.addProperty("message", "Failed to join session");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                error.toString()
            );
        }
    }

    @MessageMapping("/session/player/leave")
    public void removePlayerFromSession(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.info("Removing player from session - sessionId: {}", sessionId);
        
        try {
            sessionManager.removePlayerFromSession(sessionId);
            
            // Send confirmation to the user
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "SESSION_LEAVE_SUCCESS");
            confirmation.addProperty("message", "Successfully left session");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/game",
                confirmation.toString()
            );
        } catch (Exception e) {
            logger.error("Error removing player from session", e);
            
            // Send error to the user
            JsonObject error = new JsonObject();
            error.addProperty("type", "ERROR");
            error.addProperty("message", "Failed to leave session");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                error.toString()
            );
        }
    }
}
