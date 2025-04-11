package com.chess.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.dto.rest.request.CreateGameRequestDTO;
import com.chess.dto.rest.request.JoinGameRequestDTO;
import com.chess.dto.rest.response.GameDTO;
import com.chess.model.entity.Game;
import com.chess.service.GameService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<GameDTO> createGame(@RequestBody @Valid CreateGameRequestDTO request) {
        try {
            // Create game with parameters from the request
            Game game = gameService.createGame(
                request.getUsername(),
                request.getGameType(),
                request.getTimeControlMinutes(),
                request.getIsRated(),
                request.getCustomRules()
            );

            return ResponseEntity.ok()
                .body(GameDTO.fromGame(game));
        } catch (Exception e) {
            // Log the exception
            log.error("Error creating game", e);
        
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<GameDTO>> getAllGames() {
        try {
            // Get all games from the service
            List<Game> games = gameService.getAllGames();
            
            // Return the games with proper headers
            return ResponseEntity.ok()
                .body(games.stream()
                    .map(GameDTO::fromGame)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            // Log the error and return a 500 status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{gameId}", produces = "application/json")
    public ResponseEntity<GameDTO> getGame(@PathVariable String gameId) {
        try{
            // Get the game data from service
            Game game = gameService.getGameById(gameId);
            // Add the game data to the model so it's available in the template
            return ResponseEntity.ok(GameDTO.fromGame(game));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/join", consumes = "application/json", produces = "application/json")
    public ResponseEntity<GameDTO> joinGame(@RequestBody @Valid JoinGameRequestDTO request) {
        try{
            // Join the game
            Game game = gameService.joinGame(
                request.getGameId(), 
                request.getUsername()
            );

            return ResponseEntity.ok()
                    .body(GameDTO.fromGame(game));
        } catch (Exception e) {
            // Log the exception
            log.error("Error creating game", e);
        
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    // @MessageMapping("/game/{gameId}/chat")
    // @SendTo("/topic/game/{gameId}")
    // public ChatDTO handleChat(ChatDTO chatDTO, SimpMessageHeaderAccessor headerAccessor) {
    //     String sessionId = headerAccessor.getSessionId();
    //     String userId = sessionManager.getUserIdFromSession(sessionId);
        
    //     if (sessionManager.isSessionActive(sessionId) && userId != null) {
    //         chatDTO.setTimestamp(System.currentTimeMillis());
    //         chatDTO.setSender(userId);
    //         return chatDTO;
    //     }
    //     return null;
    // }
}
