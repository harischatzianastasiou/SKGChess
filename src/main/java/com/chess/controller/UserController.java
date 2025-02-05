package com.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.chess.model.entity.User;
import org.springframework.web.bind.annotation.RestController;
import com.chess.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@CrossOrigin(origins = "*", maxAge = 3600)

public class UserController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{userId}/games")
    public ResponseEntity<?> getUserAllGames(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getAllGames(userId));
    }

    @GetMapping("/{userId}/active-games")
    public ResponseEntity<?> getUserActiveGames(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getActiveGames(userId));
    }

    @PostMapping(value = "/req/signup") // Consumes JSON data ( automatically since it is a rest controller or else set , consumes = "application/json"), then uses @RequestBody to deserialize the JSON into a user object, Will reject any other content type (causing your 415 error)
    public ResponseEntity<User> createUser(@RequestBody User user){ // ResponseEntity Provides better control over HTTP response // Makes it clear this is a REST endpoint
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encode the password
        return ResponseEntity.ok(userService.registerUser(user)); // Register the user
    }
}
