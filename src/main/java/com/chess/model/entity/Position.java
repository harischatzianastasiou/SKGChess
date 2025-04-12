package com.chess.model.entity;

import java.time.LocalDateTime;

import com.chess.core.Alliance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "positions")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "position_id")
    private String id;  // Standard primary key name
    
    @Column(name = "position_fen", columnDefinition = "TEXT", nullable = false)
    private String fen;
    
    @Column(name = "position_moveNumber")
    private int moveNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "position_nextPlayerTurn")
    private Alliance nextPlayerTurn; // WHITE or BLACK

    @Column(name = "position_isWhiteCastled")
    private boolean isWhiteCastled;
    
    @Column(name = "position_isBlackCastled")
    private boolean isBlackCastled;
    
    @Column(name = "position_lastMovePgn")
    private String lastMovePgn;
    
    @ManyToOne
    @JoinColumn(name = "position_game_id")
    private Game game;
    
    @Column(name = "position_createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();
}