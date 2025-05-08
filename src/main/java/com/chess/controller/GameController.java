package com.chess.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.dto.rest.request.CreateGameRequestDTO;
import com.chess.dto.rest.request.JoinGameRequestDTO;
import com.chess.dto.rest.request.MakeMoveRequestDTO;
import com.chess.dto.rest.response.ErrorResponseDTO;
import com.chess.dto.rest.response.GameDTO;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.exception.UserAlreadyHasActiveGameException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createGame(@RequestBody @Valid CreateGameRequestDTO request) {
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
        } catch (UserNotFoundException e) {
            // Log the exception
            log.error("User not found when creating game: {}", e.getMessage());
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("User not found"));
        } catch (UserAlreadyHasActiveGameException e) {
            // Log the exception
            log.error("User already has an active game: {}", e.getMessage());
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Please finish or forfeit your current game before creating a new one"));
        } catch (Exception e) {
            // Log the exception with stack trace
            log.error("Error creating game: {}", e.getMessage(), e);
            // Return an error response with more details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Internal server error"));
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
    public ResponseEntity<?> joinGame(@RequestBody @Valid JoinGameRequestDTO request) {
        try {
            // Join the game
            Game game = gameService.joinGame(
                request.getGameId(), 
                request.getUsername()
            );

            // Create a message that includes both game status, board information, and player usernames
            String message = String.format(
                "{\"type\":\"GAME_STARTED\"," +
                "\"message\":\"Game is now in progress\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"IN_PROGRESS\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"whitePlayerUsername\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"blackPlayerUsername\":\"%s\"," +
                "\"boardDTO\":%s}",
                request.getGameId(),
                game.getWhitePlayer().getId(),
                game.getWhitePlayer().getUsername(),
                game.getBlackPlayer().getId(),
                game.getBlackPlayer().getUsername(),
                objectMapper.writeValueAsString(game.getBoard())
            );

            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            return ResponseEntity.ok()
                    .body(GameDTO.fromGame(game));
        } catch (UserAlreadyHasActiveGameException e) {
            // Log the exception
            log.error("User already has an active game: {}", e.getMessage());
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Please finish or forfeit your current game before joining a new one"));
        } catch (Exception e) {
            // Log the exception
            log.error("Error joining game", e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to join game"));
        }
    }

    @PostMapping(value = "/{gameId}/move", consumes = "application/json", produces = "application/json")
    public ResponseEntity<GameDTO> makeMove(@RequestBody @Valid MakeMoveRequestDTO request) {
        try {
            // Log the incoming move request
            log.info("Processing move request for game {}: from {} to {}", 
                request.getGameId(), request.getSourceCoordinate(), request.getTargetCoordinate());

            // Make the move using the service
            Game updatedGame = gameService.makeMove(
                request.getGameId(), 
                request.getSourceCoordinate(), 
                request.getTargetCoordinate()
            );
            

            // Create a message that includes both game status and board information
            String message = String.format(
            "{\"type\":\"MOVE_MADE\"," +
            "\"message\":\"Game is now in progress\"," +
            "\"gameId\":\"%s\"," +
            "\"gameStatus\":\"IN_PROGRESS\"," +
            "\"whitePlayerId\":\"%s\"," +
            "\"blackPlayerId\":\"%s\"," +
            "\"boardDTO\":%s}",
            request.getGameId(),
            updatedGame.getWhitePlayer().getId(),
            updatedGame.getBlackPlayer().getId(),
            objectMapper.writeValueAsString(updatedGame.getBoard())
            );

            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            return ResponseEntity.ok()
                    .body(GameDTO.fromGame(updatedGame));

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", request.getGameId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidMoveException e) {
            log.error("Invalid move in game {}: from {} to {}", 
                request.getGameId(), request.getSourceCoordinate(), request.getTargetCoordinate(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error making move in game {}: from {} to {}", 
                request.getGameId(), request.getSourceCoordinate(), request.getTargetCoordinate(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping(value = "/{gameId}", produces = "application/json")
    public ResponseEntity<Void> deleteGame(@PathVariable String gameId) {
        try {
            // Delete the game - service will handle all validation
            gameService.deleteGame(gameId);
            return ResponseEntity.ok().build();
        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", gameId, e);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Cannot delete game: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting game: {}", gameId, e);
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
