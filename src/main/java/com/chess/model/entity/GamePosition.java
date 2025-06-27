package com.chess.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity to store each position of a chess game for move history viewing
 * This allows players to shuffle through each move played in the game
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GAME_POSITION")
public class GamePosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "S_ID")
    private String id;
    
    // Reference to the game this position belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "S_GAME_ID", nullable = false)
    private Game game;
    
    // Move number (1, 2, 3, etc.) - represents which move this position is after
    @Column(name = "N_MOVE_NUMBER", nullable = false)
    private int moveNumber;
    
    // Serialized board state at this position
    @Column(name = "S_BOARD_STATE", columnDefinition = "TEXT", nullable = false)
    private String boardState;
    
    // Serialized move that led to this position (null for initial position)
    @Column(name = "S_MOVE_DATA", columnDefinition = "TEXT", nullable = true)
    private String moveData;
    
    // Algebraic notation for the move (e.g., "e4", "Nf3", "O-O", etc.)
    @Column(name = "S_MOVE_NOTATION", columnDefinition = "VARCHAR(20)", nullable = true)
    private String moveNotation;
    
    // Timestamp when this position was created
    @Column(name = "D_CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructor for creating a new game position
    public GamePosition(Game game, int moveNumber, String boardState, String moveData, String moveNotation) {
        this.game = game;
        this.moveNumber = moveNumber;
        this.boardState = boardState;
        this.moveData = moveData;
        this.moveNotation = moveNotation;
    }
    
    // Constructor for initial position (no move data)
    public GamePosition(Game game, String boardState) {
        this.game = game;
        this.moveNumber = 0; // Initial position
        this.boardState = boardState;
        this.moveData = null; // No move led to initial position
        this.moveNotation = null; // No move notation for initial position
    }
} 