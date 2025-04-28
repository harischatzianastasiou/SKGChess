package com.chess.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.dto.rest.response.MessageDTO;
import com.chess.exception.UserNotFoundException;
import com.chess.model.entity.Message;
import com.chess.model.entity.User;
import com.chess.repository.MessageRepository;
import com.chess.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for handling message operations.
 * Provides methods for creating, retrieving, and updating messages.
 */
@Slf4j
@Service
@Transactional
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new message for a user
     * @param gameId The ID of the game the message is about
     * @param opponentUsername The username of the opponent
     * @param content The content of the message
     * @param username The username of the user to create the message for
     * @return The created message as a DTO
     * @throws UserNotFoundException If the user is not found
     */
    public MessageDTO createMessage(String gameId, String opponentUsername, String content, String username) {
        return createMessage(gameId, opponentUsername, content, "GAME_STARTED", username);
    }

    /**
     * Create a new message for a user with a specific type
     * @param gameId The ID of the game the message is about
     * @param opponentUsername The username of the opponent
     * @param content The content of the message
     * @param type The type of the message
     * @param username The username of the user to create the message for
     * @return The created message as a DTO
     * @throws UserNotFoundException If the user is not found
     */
    public MessageDTO createMessage(String gameId, String opponentUsername, String content, String type, String username) {
        log.info("Creating message for user {} about game {}", username, gameId);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        Message message = new Message(gameId, opponentUsername, content, type, user);
        Message savedMessage = messageRepository.save(message);
        
        log.info("Message created with ID: {}", savedMessage.getId());
        return MessageDTO.fromMessage(savedMessage);
    }
    
    /**
     * Get all messages for a user
     * @param username The username of the user to get messages for
     * @return List of messages for the user
     * @throws UserNotFoundException If the user is not found
     */
    public List<MessageDTO> getMessagesForUser(String username) {
        log.info("Getting messages for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        List<Message> messages = messageRepository.findByUserOrderByTimestampDesc(user);
        
        log.info("Found {} messages for user {}", messages.size(), username);
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unread messages for a user
     * @param username The username of the user to get unread messages for
     * @return List of unread messages for the user
     * @throws UserNotFoundException If the user is not found
     */
    public List<MessageDTO> getUnreadMessagesForUser(String username) {
        log.info("Getting unread messages for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        List<Message> messages = messageRepository.findByUserAndReadFalseOrderByTimestampDesc(user);
        
        log.info("Found {} unread messages for user {}", messages.size(), username);
        return messages.stream()
                .map(MessageDTO::fromMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark all messages for a user as read
     * @param username The username of the user whose messages should be marked as read
     * @throws UserNotFoundException If the user is not found
     */
    public void markAllMessagesAsRead(String username) {
        log.info("Marking all messages as read for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        messageRepository.markAllAsRead(user);
        log.info("All messages marked as read for user {}", username);
    }
    
    /**
     * Delete all messages for a specific game
     * @param gameId The ID of the game whose messages should be deleted
     */
    public void deleteMessagesForGame(String gameId) {
        log.info("Deleting all messages for game: {}", gameId);
        messageRepository.deleteByGameId(gameId);
        log.info("All messages deleted for game {}", gameId);
    }
} 