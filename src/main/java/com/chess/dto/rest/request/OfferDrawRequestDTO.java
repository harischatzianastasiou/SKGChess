package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for offer draw requests
 * Used when a player wants to offer a draw to their opponent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDrawRequestDTO {
    
    @NotBlank(message = "Game ID is required")
    private String gameId; // The ID of the game to offer draw in
    
    @NotBlank(message = "Username is required")
    private String username; // The username of the player offering the draw
} 