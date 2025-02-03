package com.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chess.model.entity.Player;
import com.chess.service.PlayerService;

@RestController
 // @RestController automatically handles JSON responses
//  Prevents Spring from trying to find a template for the response
public class AuthController {

    private final PlayerService playerService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(PlayerService playerService, PasswordEncoder passwordEncoder) {
        this.playerService = playerService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(value = "/req/signup") // Consumes JSON data ( automatically since it is a rest controller or else set , consumes = "application/json"), then uses @RequestBody to deserialize the JSON into a Player object, Will reject any other content type (causing your 415 error)
    public ResponseEntity<Player> createUser(@RequestBody Player player){ // ResponseEntity Provides better control over HTTP response // Makes it clear this is a REST endpoint
        player.setPassword(passwordEncoder.encode(player.getPassword())); // Encode the password
        return ResponseEntity.ok(playerService.registerPlayer(player)); // Register the player
    }
} 