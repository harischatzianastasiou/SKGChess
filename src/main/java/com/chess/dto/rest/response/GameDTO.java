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
    
    /**
     * Convert a Game entity to a GameDTO
     * Automatically decompresses board data for frontend consumption
     * @param game The Game entity to convert
     * @return A new GameDTO with data from the Game entity
     */
    public static GameDTO fromGame(com.chess.model.entity.Game game) {
        // DECOMPRESS the board data before sending to frontend
        String decompressedBoard = CompressionUtil.safeDecompress(game.getBoard());
        
        return GameDTO.builder()
                .id(game.getId())
                .whitePlayerId(game.getWhitePlayer() != null ? game.getWhitePlayer().getId() : null)
                .blackPlayerId(game.getBlackPlayer() != null ? game.getBlackPlayer().getId() : null)
                .whitePlayerUsername(game.getWhitePlayer() != null ? game.getWhitePlayer().getUsername() : null)
                .blackPlayerUsername(game.getBlackPlayer() != null ? game.getBlackPlayer().getUsername() : null)
                .board(decompressedBoard) // Use decompressed board data
                .lastMoveData(game.getLastMoveData())
                .status(game.getStatus())
                .createdAt(game.getCreatedAt())
                .winnerId(game.getWinner() != null ? game.getWinner().getId() : null)
                .winnerUsername(game.getWinner() != null ? game.getWinner().getUsername() : null)
                .moveCount(game.getMoveCount())
                .isPlayerTurn(game.getIsPlayerTurn())
                .whiteTimeLeftSeconds(game.getWhiteTimeLeftSeconds())
                .blackTimeLeftSeconds(game.getBlackTimeLeftSeconds())
                .lastMoveAt(game.getLastMoveAt())
                .timeControlMinutes(game.getTimeControlMinutes())
                .build();
    }
} 