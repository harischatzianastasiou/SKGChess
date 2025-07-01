package com.chess.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.chess.model.entity.User;
import com.chess.model.entity.Game;
import com.chess.service.UserService;
import com.chess.dto.rest.request.CreateUserRequestDTO;
import com.chess.dto.rest.request.ChangeUsernameRequestDTO;
import com.chess.dto.rest.response.GameDTO;
import com.chess.dto.rest.response.UserDTO;
import com.chess.exception.UserNotFoundException;
import com.chess.exception.NewUsernameInvalidException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"https://skgchess.com", "https://www.skgchess.com", "https://skgchess.fly.dev", "http://localhost:8080"}, maxAge = 3600)
public class UserController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

//     Advantages of Using Username:
// Human-readable URLs: URLs with usernames are more intuitive and user-friendly
// SEO-friendly: Search engines can index these URLs better
// Bookmarkable: Users can bookmark profiles directly
// No need to look up IDs: Frontend doesn't need to store or retrieve IDs
// Disadvantages of Using Username:
// Security concerns: Exposing usernames in URLs might reveal information about your user base
// Performance: Username lookups are typically slower than ID lookups in databases
// Usability issues: Usernames might contain special characters that need URL encoding
// Consistency: If usernames can change, the URLs become invalid

    @GetMapping(value = "/{username}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) {
        try{
            return ResponseEntity.ok(UserDTO.fromUser(userService.getUserByUsername(username)));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequestDTO requestDTO, HttpServletRequest httpRequest) {

        try {
            // Create a new User entity from the DTO
            User user = new User();
            user.setUsername(requestDTO.getUsername());
            user.setEmail(requestDTO.getEmail());
            user.setPassword(passwordEncoder.encode(requestDTO.getPassword())); // Encode the password
            
            // Register the user
            User registeredUser = userService.registerUser(user);
            
            // Create response with user data and credentials for immediate login
            Map<String, Object> response = new HashMap<>();
            response.put("user", UserDTO.fromUser(registeredUser));
            response.put("credentials", Map.of(
                "username", requestDTO.getUsername(),
                "password", requestDTO.getPassword()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Search for users by username (for invitation system)
     * @param q The username query to search for
     * @param currentUser The current user's username to exclude from results
     * @return List of users matching the query
     */
    @GetMapping(value = "/search", produces = "application/json")
    public ResponseEntity<?> searchUsers(@RequestParam("q") String query, 
                                       @RequestParam(value = "currentUser", required = false) String currentUser) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Query parameter 'q' is required"));
            }
            
            List<UserDTO> users = userService.searchUsersByUsername(query.trim(), currentUser);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to search users"));
        }
    }

    @PostMapping(value = "/change-username", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> changeUsername(@RequestBody ChangeUsernameRequestDTO requestDTO, HttpServletRequest httpRequest) {
        try {
            User user = userService.changeUsername(requestDTO.getUsername(), requestDTO.getNewUsername());
            return ResponseEntity.ok(UserDTO.fromUser(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "User not found"));
        } catch (NewUsernameInvalidException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "The new username is not valid. Please choose another."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to change username: " + e.getMessage()));
        }
    }
}
