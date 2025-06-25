package com.chess.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.chess.core.Alliance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GAME")
public class Game implements Serializable {
    // Serializable is used to convert the object to a byte stream, so it can be sent over the network
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "S_ID")
    private String id;

    // Essential Game Information
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "S_WHITE_PLAYER_ID", nullable = false)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "S_BLACK_PLAYER_ID", nullable = true)
    private User blackPlayer;

    @Column(name = "D_CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "S_WINNER_ID")
    private User winner;

    @Column(name = "S_GAMETYPE", nullable = true)
    private String gameType = "standard";

    @Column(name = "N_TIMECONTROLMINUTES", nullable = true)
    private Integer timeControlMinutes = 10;

    @Column(name = "N_MOVECOUNT")
    private int moveCount;

    @Column(name = "S_LASTMOVEDATA")
    private String lastMoveData;

    @Column(name = "S_BOARD", columnDefinition = "TEXT", nullable = true)
    private String board;

    @Enumerated(EnumType.STRING)
    @Column(name = "S_ISPLAYERTURN", nullable = false)
    private Alliance isPlayerTurn = Alliance.WHITE;

    @NotNull
    @Column(name = "S_STATUS", nullable = false)
    private String status = GameStatus.WAITING_FOR_OPPONENT.name();

    public enum GameStatus {
        WAITING_FOR_OPPONENT,
        IN_PROGRESS,
        RESIGNED,
        CHECK,
        CHECKMATE,
        DRAW,
        STALEMATE,
        THREEFOLD_REPETITION,
        FIFTY_MOVE_RULE,
        INSUFFICIENT_MATERIAL,
        MUTUAL_AGREEMENT;
    }

    public boolean isGameStarted() {
        return status != GameStatus.WAITING_FOR_OPPONENT.name();
    }

    public void setGameStarted(boolean gameStarted) {
        if (gameStarted) {
            status = GameStatus.IN_PROGRESS.name();
        } else {
            status = GameStatus.WAITING_FOR_OPPONENT.name();
        }
    }
} 