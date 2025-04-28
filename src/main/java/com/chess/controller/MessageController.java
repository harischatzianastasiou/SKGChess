package com.chess.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.chess.dto.rest.response.MessageDTO;
import com.chess.exception.UserNotFoundException;
import com.chess.service.MessageService;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for handling message-related operations.
 * Provides endpoints for creating, retrieving, and managing messages.
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Create a new message
     * @param request The message creation request
     * @param userDetails The authenticated user
     * @return The created message
     */
    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating message for user: {}", userDetails.getUsername());
        MessageDTO message = messageService.createMessage(
            request.getGameId(),
            request.getOpponentUsername(),
            request.getContent(),
            userDetails.getUsername()
        );
        return ResponseEntity.ok(message);
    }

    /**
     * Get all unread messages for the current user
     * @param userDetails The authenticated user
     * @return List of unread messages
     */
    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting unread messages for user: {}", userDetails.getUsername());
        List<MessageDTO> messages = messageService.getUnreadMessagesForUser(userDetails.getUsername());
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark all messages as read for the current user
     * @param userDetails The authenticated user
     * @return OK response
     */
    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Marking all messages as read for user: {}", userDetails.getUsername());
        messageService.markAllMessagesAsRead(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}

/**
 * Request DTO for creating a new message
 */
class MessageRequest {
    private String gameId;
    private String opponentUsername;
    private String content;
    private String type;

    // Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getOpponentUsername() { return opponentUsername; }
    public void setOpponentUsername(String opponentUsername) { this.opponentUsername = opponentUsername; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 