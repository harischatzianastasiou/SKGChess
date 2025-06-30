package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for responding to draw offers
 * Used when a player accepts or declines a draw offer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondToDrawRequestDTO {
    
    @NotBlank(message = "Game ID is required")
    private String gameId; // The ID of the game to respond to draw offer in
    
    @NotBlank(message = "Username is required")
    private String username; // The username of the player responding to the draw offer
    
    @NotBlank(message = "Response action is required")
    private String action; // "accept" or "decline"
} 