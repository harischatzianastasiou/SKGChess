package com.chess.dto.rest.response;

import java.time.LocalDateTime;

import com.chess.core.Alliance;
import com.chess.core.board.IBoard;
import com.chess.util.CompressionUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Game entity
 * Used to avoid circular references in JSON serialization
 * Automatically decompresses board data for frontend consumption
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    // Game ID
    private String id;
    
    // Player IDs (not the full User objects)
    private String whitePlayerId;
    private String blackPlayerId;
    
    // Player usernames for display
    private String whitePlayerUsername;
    private String blackPlayerUsername;
    
    // Game state
    private String board;
    private String lastMoveData;
    private String status;
    private LocalDateTime createdAt;
    
    // Game result
    private String winnerId;
    private String winnerUsername;
    
    // Game statistics
    private int moveCount;
    
    // Current player turn
    private Alliance isPlayerTurn;
    
    // Timer-related fields
    private Integer whiteTimeLeftSeconds;
    private Integer blackTimeLeftSeconds;
    private LocalDateTime lastMoveAt;
    private Integer timeControlMinutes;
    
    // The current server time when the response is generated
    private LocalDateTime serverTime; // Used for client-server time sync
    
    /**
     * Convert a Game entity to a GameDTO
     * Automatically decompresses board data for frontend consumption
     * @param game The Game entity to convert
     * @return A new GameDTO with data from the Game entity
     */
    public static GameDTO fromGame(com.chess.model.entity.Game game) {
        // DECOMPRESS the board data before sending to frontend
        String decompressedBoard = CompressionUtil.safeDecompress(game.board); // fallback to field access if Lombok getters are not recognized
        // Use direct field access as a workaround for environments where Lombok annotation processing is not working as expected
        return GameDTO.builder()
                .id(game.id)
                .whitePlayerId(game.whitePlayer != null ? game.whitePlayer.id : null)
                .blackPlayerId(game.blackPlayer != null ? game.blackPlayer.id : null)
                .whitePlayerUsername(game.whitePlayer != null ? game.whitePlayer.username : null)
                .blackPlayerUsername(game.blackPlayer != null ? game.blackPlayer.username : null)
                .board(decompressedBoard) // Use decompressed board data
                .lastMoveData(game.lastMoveData)
                .status(game.status)
                .createdAt(game.createdAt)
                .winnerId(game.winner != null ? game.winner.id : null)
                .winnerUsername(game.winner != null ? game.winner.username : null)
                .moveCount(game.moveCount)
                .isPlayerTurn(game.isPlayerTurn)
                .whiteTimeLeftSeconds(game.whiteTimeLeftSeconds)
                .blackTimeLeftSeconds(game.blackTimeLeftSeconds)
                .lastMoveAt(game.lastMoveAt)
                .timeControlMinutes(game.timeControlMinutes)
                // .serverTime is NOT set here; it will be set in the controller for accurate response time
                .build();
    }
} 