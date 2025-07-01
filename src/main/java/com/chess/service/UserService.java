package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.chess.exception.UserNotFoundException;
import com.chess.model.entity.User;
import com.chess.repository.UserRepository;
import com.chess.dto.rest.response.UserDTO;
import com.chess.exception.NewUsernameInvalidException;
import com.chess.exception.UsernameChangesLeftException;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        // Create a list of authorities (roles) for the user
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Add default user role

        // Build and return the UserDetails object with proper authorities and account status
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }

    public User registerUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    /**
     * Search for users by username (for invitation system)
     * @param query The username query to search for
     * @param currentUsername The current user's username to exclude from results
     * @return List of users matching the query
     */
    public List<UserDTO> searchUsersByUsername(String query, String currentUsername) {
        // Use the repository method for better performance
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseAndUsernameNot(
            query, 
            currentUsername != null ? currentUsername : ""
        );
        
        return users.stream()
            .limit(10) // Limit to 10 results
            .map(UserDTO::fromUser)
            .collect(Collectors.toList());
    }

    public User changeUsername(String username, String newUsername) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getUsernameChangesLeft() <= 0) {
            throw new UsernameChangesLeftException("You have reached the maximum number of username changes.");
        }
        if (newUsername.equals(username) || newUsername.isEmpty() || newUsername.isBlank()) {
            throw new NewUsernameInvalidException("New username cannot be the same as the current username or empty");
        }

        user.setUsername(newUsername);
        user.setUsernameChangesLeft(user.getUsernameChangesLeft() - 1);
        return userRepository.save(user);
    }
}
