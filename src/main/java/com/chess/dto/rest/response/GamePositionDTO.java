package com.chess.dto.rest.response;

import java.time.LocalDateTime;

import com.chess.core.board.IBoard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for GamePosition entity
 * Used to transfer position data to frontend for move history viewing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePositionDTO {
    
    // Position identifier
    private String id;
    private int moveNumber;
    
    // Board state at this position
    private IBoard board;
    
    // Move that led to this position (null for initial position)
    private String moveData;
    
    // Timestamp when this position was created
    private LocalDateTime createdAt;
    
    // Game information
    private String gameId;
    
    // Move notation for display (e.g., "e4", "Nf3", etc.)
    private String moveNotation;
    
    // Player who made this move (null for initial position)
    private String playerAlliance; // "WHITE" or "BLACK"
} 