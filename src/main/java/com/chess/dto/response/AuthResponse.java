package com.chess.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String id;
    private String username;
    private String email;
    private String token;
    private String type = "Bearer";
    
    public AuthResponse(String id, String username, String email, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.token = token;
    }
} 