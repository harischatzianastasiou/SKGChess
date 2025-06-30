package com.chess.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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
import com.chess.dto.rest.request.ResignGameRequestDTO;
import com.chess.dto.rest.request.OfferDrawRequestDTO;
import com.chess.dto.rest.request.RespondToDrawRequestDTO;
import com.chess.dto.rest.request.OfferRematchRequestDTO;
import com.chess.dto.rest.request.RespondToRematchRequestDTO;
import com.chess.dto.rest.response.ErrorResponseDTO;
import com.chess.dto.rest.response.GameDTO;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.InvalidMoveException;
import com.chess.exception.UserNotFoundException;
import com.chess.exception.UserAlreadyHasActiveGameException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;
import com.chess.service.GameService;
import com.chess.util.CompressionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = {"https://skgchess.com", "https://www.skgchess.com", "https://skgchess.fly.dev", "http://localhost:8080"}, maxAge = 3600)
public class GameController {
    
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;// to convert game object to json for websocket communication

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createGame(@RequestBody @Valid CreateGameRequestDTO request, 
                                      HttpServletRequest httpRequest) {
        try {

            // Create the game
            Game game = gameService.createGame(
                request.getUsername(), 
                request.getGameType(), 
                request.getTimeControlMinutes(),
                request.getIncrementSeconds(),
                request.getIsRated(),
                request.getCustomRules(),
                request.getPlayerColor()
            );

            // Return the created game
            GameDTO gameDTO = GameDTO.fromGame(game);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                .body(gameDTO);

        } catch (UserAlreadyHasActiveGameException e) {
            // Log the exception
            log.error("User already has an active game: {}", e.getMessage());
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("You must finish your current game before playing a new one"));
        } catch (Exception e) {
            // Log the exception with stack trace
            log.error("Error creating game: {}", e.getMessage(), e);
            // Return an error response
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
            // Create GameDTO and set server time for client-server time sync
            GameDTO gameDTO = GameDTO.fromGame(game);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            // Add the game data to the model so it's available in the template
            return ResponseEntity.ok(gameDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/join", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> joinGame(@RequestBody @Valid JoinGameRequestDTO request) {
        try {
            // Log the incoming join request
            log.info("Processing join request for game {} by user {}", 
                request.getGameId(), request.getUsername());

            // Join the game
            Game game = gameService.joinGame(
                request.getGameId(), 
                request.getUsername()
            );

            // DECOMPRESS the board before sending to frontend
            String decompressedBoard = CompressionUtil.safeDecompress(game.getBoard());

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
                game.getWhitePlayer() != null ? game.getWhitePlayer().getId() : "",
                game.getWhitePlayer() != null ? game.getWhitePlayer().getUsername() : "",
                game.getBlackPlayer() != null ? game.getBlackPlayer().getId() : "",
                game.getBlackPlayer() != null ? game.getBlackPlayer().getUsername() : "",
                objectMapper.writeValueAsString(decompressedBoard)
            );

            // Send the message to the game topic
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(game);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (UserAlreadyHasActiveGameException e) {
            // Log the exception
            log.error("User already has an active game: {}", e.getMessage());
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Please finish or forfeit your current game before joining a new one"));
        } catch (GameNotFoundException e) {
            // Log the exception
            log.error("Game not found: {}", request.getGameId(), e);
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("Game not found"));
        } catch (Exception e) {
            // Log the exception with stack trace
            log.error("Error joining game: {}", e.getMessage(), e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to join game: " + e.getMessage()));
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
            
            // DECOMPRESS the board before sending to frontend
            String decompressedBoard = CompressionUtil.safeDecompress(updatedGame.getBoard());

            // Parse the lastMoveData to get the moveType for sound effects
            String moveType = "NORMAL"; // Default move type
            if (updatedGame.getLastMoveData() != null && !updatedGame.getLastMoveData().isEmpty()) {
                try {
                    JsonNode moveDataNode = objectMapper.readTree(updatedGame.getLastMoveData());
                    if (moveDataNode.has("moveType")) {
                        moveType = moveDataNode.get("moveType").asText();
                        log.info("Move type for sound effect: {}", moveType);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse lastMoveData for moveType, using default: {}", e.getMessage());
                }
            }

            // Create a message that includes both game status, board information, and move type for sound effects
            String whitePlayerId = updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "";
            String blackPlayerId = updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "";
            String winnerId = updatedGame.getWinner() != null ? updatedGame.getWinner().getId() : "";
            
            // Debug logging
            log.info("WebSocket message debug - WhitePlayer: {}, BlackPlayer: {}, Winner: {}, GameStatus: {}", 
                whitePlayerId, blackPlayerId, winnerId, updatedGame.getStatus());
            
            String message = String.format(
            "{\"type\":\"MOVE_MADE\"," +
            "\"message\":\"Game is now in progress\"," +
            "\"gameId\":\"%s\"," +
            "\"gameStatus\":\"%s\"," +
            "\"whitePlayerId\":\"%s\"," +
            "\"blackPlayerId\":\"%s\"," +
            "\"winnerId\":\"%s\"," +
            "\"moveType\":\"%s\"," +
            "\"boardDTO\":%s}",
            request.getGameId(),
            updatedGame.getStatus(),
            whitePlayerId,
            blackPlayerId,
            winnerId,
            moveType,
            objectMapper.writeValueAsString(decompressedBoard)
            );

            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

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

    @PostMapping(value = "/{gameId}/timeout", produces = "application/json")
    public ResponseEntity<GameDTO> handleTimeout(@PathVariable String gameId) {
        try {
            // Log the timeout request
            log.info("Processing timeout request for game: {}", gameId);

            // Handle the timeout using the service
            Game updatedGame = gameService.handleTimeout(gameId);

            // Send WebSocket notification to both players about the timeout
            String message = String.format(
                "{\"type\":\"TIME_OUT\"," +
                "\"message\":\"Game ended by timeout\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"TIME_OUT\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"winnerId\":\"%s\"}",
                gameId,
                updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "",
                updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "",
                updatedGame.getWinner() != null ? updatedGame.getWinner().getId() : ""
            );

            // Send the timeout message to both players via WebSocket
            messagingTemplate.convertAndSend("/topic/game/" + gameId, message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            // Log the exception
            log.error("Game not found: {}", gameId, e);
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GameDTO()); // Return empty game DTO for not found
        } catch (Exception e) {
            // Log the exception with stack trace
            log.error("Error handling timeout: {}", e.getMessage(), e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GameDTO()); // Return empty game DTO for error
        }
    }

    @PostMapping(value = "/{gameId}/start-timer", produces = "application/json")
    public ResponseEntity<GameDTO> startGameTimer(@PathVariable String gameId) {
        try {
            // Log the timer start request
            log.info("Starting timer for game: {}", gameId);

            // Start the timer using the service
            Game updatedGame = gameService.startGameTimer(gameId);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            // Log the exception
            log.error("Game not found: {}", gameId, e);
            // Return a more specific error response
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GameDTO()); // Return empty game DTO for not found
        } catch (Exception e) {
            // Log the exception with stack trace
            log.error("Error starting game timer: {}", e.getMessage(), e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GameDTO()); // Return empty game DTO for error
        }
    }

    /**
     * Resign a game - the player who resigns loses, the opponent wins
     * @param request The resign request containing game ID and username
     * @return The updated game state
     */
    @PostMapping(value = "/resign", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> resignGame(@RequestBody @Valid ResignGameRequestDTO request) {
        try {
            // Log the resign request
            log.info("Processing resign request for game {} by user {}", 
                request.getGameId(), request.getUsername());

            // Resign the game using the service
            Game updatedGame = gameService.resignGame(
                request.getGameId(), 
                request.getUsername()
            );

            // Send WebSocket notification to both players about the resignation
            String message = String.format(
                "{\"type\":\"GAME_RESIGNED\"," +
                "\"message\":\"Game ended by resignation\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"RESIGNED\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"winnerId\":\"%s\"}",
                request.getGameId(),
                updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "",
                updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "",
                updatedGame.getWinner() != null ? updatedGame.getWinner().getId() : ""
            );

            // Send the resignation message to both players via WebSocket
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", request.getGameId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("Game not found"));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("User not found"));
        } catch (IllegalStateException e) {
            log.error("Invalid resign request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error resigning game: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to resign game"));
        }
    }

    /**
     * Offer a draw to the opponent
     * @param request The draw offer request containing game ID and username
     * @return The updated game state
     */
    @PostMapping(value = "/offer-draw", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> offerDraw(@RequestBody @Valid OfferDrawRequestDTO request) {
        try {
            // Log the draw offer request
            log.info("Processing draw offer request for game {} by user {}", 
                request.getGameId(), request.getUsername());

            // Offer the draw using the service
            Game updatedGame = gameService.offerDraw(
                request.getGameId(), 
                request.getUsername()
            );

            // Send WebSocket notification to both players about the draw offer
            String message = String.format(
                "{\"type\":\"DRAW_OFFERED\"," +
                "\"message\":\"Draw offer made\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"%s\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"offeringPlayerUsername\":\"%s\"}",
                request.getGameId(),
                updatedGame.getStatus(), // Use current game status instead of "DRAW_OFFERED"
                updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "",
                updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "",
                request.getUsername()
            );

            // Send the draw offer message to both players via WebSocket
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", request.getGameId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("Game not found"));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("User not found"));
        } catch (IllegalStateException e) {
            log.error("Invalid draw offer request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error offering draw: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to offer draw"));
        }
    }

    /**
     * Respond to a draw offer (accept or decline)
     * @param request The draw response request containing game ID, username, and action
     * @return The updated game state
     */
    @PostMapping(value = "/respond-draw", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> respondToDrawOffer(@RequestBody @Valid RespondToDrawRequestDTO request) {
        try {
            // Log the draw response request
            log.info("Processing draw response for game {} by user {} with action: {}", 
                request.getGameId(), request.getUsername(), request.getAction());

            // Respond to the draw offer using the service
            Game updatedGame = gameService.respondToDrawOffer(
                request.getGameId(), 
                request.getUsername(),
                request.getAction()
            );

            // Send WebSocket notification to both players about the draw response
            String message = String.format(
                "{\"type\":\"DRAW_RESPONSE\"," +
                "\"message\":\"Draw offer %s\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"%s\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"winnerId\":\"%s\"," +
                "\"respondingPlayerUsername\":\"%s\"," +
                "\"action\":\"%s\"}",
                request.getAction(),
                request.getGameId(),
                updatedGame.getStatus(),
                updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "",
                updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "",
                updatedGame.getWinner() != null ? updatedGame.getWinner().getId() : "",
                request.getUsername(),
                request.getAction()
            );

            // Send the draw response message to both players via WebSocket
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", request.getGameId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("Game not found"));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("User not found"));
        } catch (IllegalStateException e) {
            log.error("Invalid draw response request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid action in draw response: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error responding to draw offer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to respond to draw offer"));
        }
    }

    /**
     * Offer a rematch to the opponent
     * @param request The rematch offer request containing game ID and username
     * @return The updated game state
     */
    @PostMapping(value = "/offer-rematch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> offerRematch(@RequestBody @Valid OfferRematchRequestDTO request) {
        try {
            // Log the rematch offer request
            log.info("Processing rematch offer request for game {} by user {}", 
                request.getGameId(), request.getUsername());

            // Offer the rematch using the service
            Game updatedGame = gameService.offerRematch(
                request.getGameId(), 
                request.getUsername()
            );

            // Send WebSocket notification to both players about the rematch offer
            String message = String.format(
                "{\"type\":\"REMATCH_OFFERED\"," +
                "\"message\":\"Rematch offer made\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"%s\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"offeringPlayerUsername\":\"%s\"}",
                request.getGameId(),
                updatedGame.getStatus(),
                updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "",
                updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "",
                request.getUsername()
            );

            // Send the rematch offer message to both players via WebSocket
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", request.getGameId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("Game not found"));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("User not found"));
        } catch (IllegalStateException e) {
            log.error("Invalid rematch offer request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error offering rematch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to offer rematch"));
        }
    }

    /**
     * Respond to a rematch offer (accept or decline)
     * @param request The rematch response request containing game ID, username, and action
     * @return The updated game state or new game if accepted
     */
    @PostMapping(value = "/respond-rematch", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> respondToRematchOffer(@RequestBody @Valid RespondToRematchRequestDTO request) {
        try {
            // Log the rematch response request
            log.info("Processing rematch response for game {} by user {} with action: {}", 
                request.getGameId(), request.getUsername(), request.getAction());

            // Respond to the rematch offer using the service
            Game updatedGame = gameService.respondToRematchOffer(
                request.getGameId(), 
                request.getUsername(),
                request.getAction()
            );

            // Send WebSocket notification to both players about the rematch response
            String message = String.format(
                "{\"type\":\"REMATCH_RESPONSE\"," +
                "\"message\":\"Rematch offer %s\"," +
                "\"gameId\":\"%s\"," +
                "\"gameStatus\":\"%s\"," +
                "\"whitePlayerId\":\"%s\"," +
                "\"blackPlayerId\":\"%s\"," +
                "\"newGameId\":\"%s\"," +
                "\"respondingPlayerUsername\":\"%s\"," +
                "\"action\":\"%s\"}",
                request.getAction(),
                request.getGameId(),
                updatedGame.getStatus(),
                updatedGame.getWhitePlayer() != null ? updatedGame.getWhitePlayer().getId() : "",
                updatedGame.getBlackPlayer() != null ? updatedGame.getBlackPlayer().getId() : "",
                updatedGame.getId(),
                request.getUsername(),
                request.getAction()
            );

            // Send the rematch response message to both players via WebSocket
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId(), message);

            // Return the updated game state
            GameDTO gameDTO = GameDTO.fromGame(updatedGame);
            gameDTO.setServerTime(LocalDateTime.now()); // Set current server time for client sync
            return ResponseEntity.ok()
                    .body(gameDTO);

        } catch (GameNotFoundException e) {
            log.error("Game not found: {}", request.getGameId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("Game not found"));
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("User not found"));
        } catch (IllegalStateException e) {
            log.error("Invalid rematch response request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid action in rematch response: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error responding to rematch offer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to respond to rematch offer"));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
