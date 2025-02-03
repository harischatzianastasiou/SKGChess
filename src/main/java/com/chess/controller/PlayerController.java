package com.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.chess.service.PlayerService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
@RequestMapping("/player")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlayerController {
    
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // @PostMapping("/signup")
    // public ResponseEntity<?> registerPlayer(@Valid @RequestBody SignupRequest signupRequest) {
    //     return ResponseEntity.ok(playerService.registerPlayer(signupRequest.getUsername(), signupRequest.getEmail(), signupRequest.getPassword()));
    // }

    // @PostMapping("/login")
    // public ResponseEntity<?> authenticatePlayer(@Valid @RequestBody LoginRequest loginRequest) {
    //     return ResponseEntity.ok(playerService.getPlayerByUsernameOrEmail(loginRequest.getUsernameOrEmail()));
    // }

    // @GetMapping("/{playerId}")
    // public ResponseEntity<Player> getPlayer(@PathVariable String playerId) {
    //     return ResponseEntity.ok(playerService.getPlayerById(playerId));
    // }

    @GetMapping("/{playerId}/games")
    public ResponseEntity<?> getPlayerAllGames(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.getAllGames(playerId));
    }

    @GetMapping("/{playerId}/active-games")
    public ResponseEntity<?> getPlayerActiveGames(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.getActiveGames(playerId));
    }

    @MessageMapping("/chat/send")
    @SendTo("/topic/messages")
    public String handleChatMessage(String messageJson) {

        JsonObject jsonObject = JsonParser.parseString(messageJson).getAsJsonObject();
        String content = jsonObject.get("content").getAsString();
        String timestamp = jsonObject.get("timestamp").getAsString();
        return String.format("[%s] %s", timestamp, content);
    }
}
