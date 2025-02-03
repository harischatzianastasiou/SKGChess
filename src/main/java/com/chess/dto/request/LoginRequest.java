package com.chess.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Username/email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }
} 