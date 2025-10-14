package com.chess.dto.rest.request;

import com.chess.validation.ValidEmail;
import com.chess.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration requests
 * Used to validate and transfer user registration data with enhanced security validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {
    // Username field with validation
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    // Email field with enhanced validation
    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Invalid email format. Please provide a valid email address.")
    private String email;
    
    // Password field with strong validation
    @NotBlank(message = "Password is required")
    @ValidPassword(message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;
} 