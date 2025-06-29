package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGameRequestDTO {
    // Required field with validation
    @NotBlank(message = "Username is required")
    private String username;
    
    // Optional field with default value
    @Builder.Default
    private String gameType = "standard";
    
    // Optional field for time control
    private Integer timeControlMinutes;
    
    // Optional field for game settings
    private Boolean isRated = false;
    
    // Optional field for custom rules
    private String customRules;

    private String playerColor;

    // Optional field for increment (seconds per move)
    private Integer incrementSeconds;
}