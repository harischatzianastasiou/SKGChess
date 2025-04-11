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
import com.chess.model.entity.User;
import com.chess.model.entity.Game;
import com.chess.service.UserService;
import com.chess.dto.rest.request.CreateUserRequestDTO;
import com.chess.dto.rest.response.GameDTO;
import com.chess.dto.rest.response.UserDTO;
import com.chess.exception.UserNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
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

    @GetMapping(value = "/{userId}/games", produces = "application/json")
    public ResponseEntity<List<GameDTO>> getUserAllGames(@PathVariable String userId) {
        // Get all games from the service
        List<Game> games = userService.getAllGames(userId);

        // Return the games with proper headers
        return ResponseEntity.ok()
            .body(games.stream()
                .map(GameDTO::fromGame)
                .collect(Collectors.toList()));
    }

    @GetMapping(value = "/{userId}/active-games", produces = "application/json")
    public ResponseEntity<List<GameDTO>> getUserActiveGames(@PathVariable String userId) {
        // Get all active games from the service
        List<Game> games = userService.getActiveGames(userId);

        // Return the games with proper headers
        return ResponseEntity.ok()
            .body(games.stream()
                .map(GameDTO::fromGame)
                .collect(Collectors.toList()));
    }

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequestDTO requestDTO){
        // Create a new User entity from the DTO
        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword())); // Encode the password
        
        // Register the user and return the DTO
        return ResponseEntity.ok(UserDTO.fromUser(userService.registerUser(user))); // Register the user
    }
}
