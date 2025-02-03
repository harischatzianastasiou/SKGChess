package com.chess.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", 
            message = "Username must be between 3 and 20 characters and can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must be at least 8 characters long and include at least one letter and one number")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
} 