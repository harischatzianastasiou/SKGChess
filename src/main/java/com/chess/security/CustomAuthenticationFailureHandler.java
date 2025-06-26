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
        
        // Provide specific error messages based on the type of authentication failure
        String errorMessage;
        if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid username or password. Please check your credentials and try again.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Your account has been disabled. Please contact support for assistance.";
        } else if (exception instanceof LockedException) {
            errorMessage = "Your account has been locked. Please contact support for assistance.";
        } else {
            errorMessage = "Authentication failed. Please check your credentials and try again.";
        }
        
        errorResponse.put("message", errorMessage);
        
        // Write JSON response
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
} 