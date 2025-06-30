package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for offer rematch requests
 * Used when a player wants to offer a rematch to their opponent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferRematchRequestDTO {
    
    @NotBlank(message = "Game ID is required")
    private String gameId; // The ID of the game to offer rematch in
    
    @NotBlank(message = "Username is required")
    private String username; // The username of the player offering the rematch
} 