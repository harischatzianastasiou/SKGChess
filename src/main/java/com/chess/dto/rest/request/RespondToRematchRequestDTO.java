package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for responding to rematch offers
 * Used when a player accepts or declines a rematch offer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondToRematchRequestDTO {
    
    @NotBlank(message = "Game ID is required")
    private String gameId; // The ID of the game to respond to rematch offer in
    
    @NotBlank(message = "Username is required")
    private String username; // The username of the player responding to the rematch offer
    
    @NotBlank(message = "Response action is required")
    private String action; // "accept" or "decline"
} 