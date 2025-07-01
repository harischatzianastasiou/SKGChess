package com.chess.dto.rest.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entity
 * Used to transfer user data without exposing sensitive information like passwords
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    // User ID
    private String id;
    
    // User credentials (excluding password for security)
    private String username;
    private String email;
    
    // User statistics
    private int rating;
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int gamesDraw;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    private int usernameChangesLeft;
    
    /**
     * Convert a User entity to a UserDTO
     * @param user The User entity to convert
     * @return A new UserDTO with data from the User entity
     */
    public static UserDTO fromUser(com.chess.model.entity.User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .usernameChangesLeft(user.getUsernameChangesLeft())
                .build();
    }
}
