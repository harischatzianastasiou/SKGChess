package com.chess.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.chess.model.session.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.chess.service.GameService;
import com.chess.model.entity.Game;

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
    @SendTo("/topic/session/queue/join")
    public String joinMatchmakingQueue(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        //payload is the data sent from the client to the server.
        //headerAccessor is used to get the session ID of the client (websocket session)
        String username = payload.get("username").asText();
        String sessionId = headerAccessor.getSessionId();
        logger.info("Adding user to matchmaking queue - username: {}, sessionId: {}", username, sessionId);

        try {
            sessionManager.addToMatchmakingQueue(sessionId, username);
            if(sessionManager.isGameCreated()) { // alliws me gamecontroller kai subscribe
                Game game = gameService.createGame(sessionManager.getUserIdFromSession(sessionId), sessionManager.getUserIdFromSession(sessionId));
                sessionManager.resetGameCreated();
                sessionManager.removeFromMatchmakingQueue(sessionId);
                return "Server Responded : Game created:" + game.getId();
            }
            else {
                // Send confirmation to the user
                return "Server Responded : Game not created";
            }
        } catch (Exception e) {
            logger.error("Error adding user to queue", e);
            return "Server Responded : Error adding user to queue";
        }
    }


    @MessageMapping("/session/queue/leave")
    public void leaveFromMatchmakingQueue(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.info("Removing user from matchmaking queue - sessionId: {}", sessionId);
        

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
            logger.error("Error removing user from queue", e);
            
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

    @MessageMapping("/session/UserToSession/add")
    @SendTo("/topic/{sessionId}/add")
    public void addUserToSession(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = payload.get("userId").asText();
        logger.info("Adding user to session - userId: {}, sessionId: {}", userId, sessionId);
        
        try {
            sessionManager.addUserToSession(sessionId, userId);
            
            // Send confirmation to the user
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "SESSION_JOIN_SUCCESS");
            confirmation.addProperty("message", "Successfully joined session");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/session/userToSession/add",
                confirmation.toString()
            );
        } catch (Exception e) {
            logger.error("Error adding user to session", e);
            
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

    @MessageMapping("/session/userToSession/leave")
    public void removeuserFromSession(@Payload JsonNode payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.info("Removing user from session - sessionId: {}", sessionId);
        
        try {
            sessionManager.removeUserFromSession(sessionId);
            
            // Send confirmation to the user
            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("type", "SESSION_LEAVE_SUCCESS");
            confirmation.addProperty("message", "Successfully left session");
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/session/userToSession/leave",
                confirmation.toString()
            );
        } catch (Exception e) {
            logger.error("Error removing user from session", e);
            
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
