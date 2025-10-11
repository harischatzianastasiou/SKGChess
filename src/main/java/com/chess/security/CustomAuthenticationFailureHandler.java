package com.chess.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        // Set response status and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        
        // Create error response with specific message based on exception type
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        
        // Provide specific error codes based on the type of authentication failure
        String errorCode;
        if (exception instanceof BadCredentialsException) {
            errorCode = "invalidCredentials";
        } else if (exception instanceof DisabledException) {
            errorCode = "accountDisabled";
        } else if (exception instanceof LockedException) {
            errorCode = "accountLocked";
        } else {
            errorCode = "authenticationFailed";
        }
        
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", errorCode); // Frontend will translate this
        
        // Write JSON response
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
} 