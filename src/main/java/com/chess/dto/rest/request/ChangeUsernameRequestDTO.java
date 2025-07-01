package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeUsernameRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "New username is required")
    private String newUsername;
}