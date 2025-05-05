package com.chess.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.chess.model.entity.User;
import com.chess.service.UserService;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load the default OAuth2 user
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Get user attributes from OAuth2 provider
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extract email and name from attributes
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        
        try {
            // Try to find existing user by email
            User user = userService.getUserByUsernameOrEmail(email);
            String username;
            
            if (user != null) {
                // Use existing user's username
                username = user.getUsername();
            } else {
                // Create new user with Google name as username
                username = name != null ? name.replaceAll("\\s+", "").toLowerCase() 
                                      : "user" + UUID.randomUUID().toString().substring(0, 8);
                User newUser = new User(username, email);
                newUser.setPassword(UUID.randomUUID().toString()); // Generate a random password
                userService.registerUser(newUser);
            }
            
            // Create new attributes map with the correct username
            Map<String, Object> newAttributes = new HashMap<>(attributes);
            newAttributes.put("name", username); // Override the name attribute with our username
            
            // Return a new OAuth2User with the correct username
            return new DefaultOAuth2User(oAuth2User.getAuthorities(), newAttributes, "name");
            
        } catch (Exception e) {
            // Log the error and rethrow
            System.err.println("Error during OAuth2 user processing: " + e.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error", e.getMessage(), null), e);
        }
    }
} 