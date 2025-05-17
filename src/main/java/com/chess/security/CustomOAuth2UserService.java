package com.chess.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.chess.model.entity.User;
import com.chess.service.UserService;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // Load the default OAuth2 user
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            // Get user attributes from OAuth2 provider
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            // Extract email and name from attributes
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            
            if (email == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error("missing_email", "Email is required", null));
            }
            
            // Try to find existing user by email
            User user = null;
            try {
                user = userService.getUserByEmail(email);
            } catch (Exception e) {
                // User not found, will create new one
            }
            
            String username;
            if (user != null) {
                // Use existing user's username
                username = user.getUsername();
            } else {
                // Create new user with Google name as username
                username = name != null ? name.replaceAll("\\s+", "").toLowerCase() 
                                      : "user" + UUID.randomUUID().toString().substring(0, 8);
                
                User newUser = new User(username, email);
                // Generate and encode a random password
                String randomPassword = UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(randomPassword));
                
                try {
                    userService.registerUser(newUser);
                } catch (Exception e) {
                    throw e;
                }
            }
            
            // Create new attributes map with the correct username
            Map<String, Object> newAttributes = new HashMap<>(attributes);
            newAttributes.put("name", username); // Override the name attribute with our username
            
            // Return a new OAuth2User with the correct username
            return new DefaultOAuth2User(oAuth2User.getAuthorities(), newAttributes, "name");
            
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error", e.getMessage(), null), e);
        }
    }
} 