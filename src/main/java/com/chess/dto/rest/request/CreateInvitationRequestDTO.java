package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationRequestDTO {
    // Required field with validation - username of the person creating the invitation
    @NotBlank(message = "Username is required")
    private String username;
    
    // Required field - username of the opponent to invite
    @NotBlank(message = "Opponent username is required")
    private String opponentUsername;
    
    // Required field for time control with validation
    @NotNull(message = "Time control is required")
    @Min(value = 1, message = "Time control must be at least 1 minute")
    @Max(value = 60, message = "Time control must be at most 60 minutes")
    private Integer timeControlMinutes;
    
    // Optional field for player color preference
    private String playerColor; // "white", "black", or "random"
} 