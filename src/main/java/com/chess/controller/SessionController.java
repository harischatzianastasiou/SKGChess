package com.chess.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.chess.model.session.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.chess.service.GameService;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600) //Allows cross-origin requests, which is useful for enabling WebSocket connections from different domains.
public class SessionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final SessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    public SessionController(SessionManager sessionManager, SimpMessagingTemplate messagingTemplate, GameService gameService) {
        this.sessionManager = sessionManager;
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
    }

    @MessageMapping("/session/queue/join")
    //@SendTo is particularly useful for public messages that ALL clients should receive. But here, we are sending a message to a specific user identified by their session ID. That's why we dont use @SendTo and use messagingTemplate.convertAndSendToUser(...) instead.
    public void joinMatchmakingQueue(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        //payload is the data sent from the client to the server.
        //headerAccessor is used to get the session ID of the client (websocket session)
        String username = payload.get("username").asText();
        String sessionId = headerAccessor.getSessionId();
        logger.info("Adding player to matchmaking queue - username: {}, sessionId: {}", username, sessionId);

        try {
            sessionManager.addToMatchmakingQueue(sessionId, username);
            JsonObject confirmation = new JsonObject();
            if(sessionManager.isGameCreated()) { // alliws me gamecontroller kai subscribe
                gameService.createGame(sessionManager.getPlayerIdFromSession(sessionId), sessionManager.getPlayerIdFromSession(sessionId));
                sessionManager.resetGameCreated();
                sessionManager.removeFromMatchmakingQueue(sessionId);
                confirmation.addProperty("type", "GAME_CREATED");
                confirmation.addProperty("message", "Successfully joined matchmaking queue and created a game");
                confirmation.addProperty("gameId", gameService.getGameById(sessionManager.getPlayerIdFromSession(sessionId)).getId());
            }
            else {
                // Send confirmation to the user
                confirmation.addProperty("type", "QUEUE_JOIN_SUCCESS");
                confirmation.addProperty("message", "Successfully joined matchmaking queue");
            }
            /* When you see messagingTemplate.convertAndSendToUser(...), it indicates that you are sending a message to a specific user identified by their session ID. */
            messagingTemplate.convertAndSendToUser( //a confirmation message is sent back to the user who initiated the request.
                sessionId,
                "/session/queue/join",
                confirmation.toString()
            );//returns in json format

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

    @MessageMapping("session/queue/leave")
    public void leaveFromMatchmakingQueue(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
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
                "/session/queue/leave",
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

    @MessageMapping("/session/playerToSession/add")
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
                "/session/playerToSession/add",
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

    @MessageMapping("session/playerToSession/leave")
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
                "/session/playerToSession/leave",
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
