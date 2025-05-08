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
@Table(name = "games")
public class Game implements Serializable {
    // Serializable is used to convert the object to a byte stream, so it can be sent over the network
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "game_id")
    private String id;

    // Essential Game Information
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_user_whiteId", nullable = false)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_user_blackId", nullable = true)
    private User blackPlayer;

    @NotNull
    @Column(name = "game_status", nullable = false)
    private String status = GameStatus.WAITING_FOR_OPPONENT.name();

    @Column(name = "game_createdAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "game_winnerId")
    private User winner;

    @Column(name = "game_type", nullable = true)
    private String gameType = "standard";

    @Column(name = "game_timeControlMinutes", nullable = true)
    private Integer timeControlMinutes = 10;

    @Column(name = "game_isRated", nullable = true)
    private Boolean isRated = true;

    @Column(name = "game_customRules", columnDefinition = "TEXT", nullable = true)
    private String customRules = "";

    // Game State Information
    @Column(name = "game_fenPosition", columnDefinition = "TEXT", nullable = true)
    private String fenPosition;

    @Column(name = "game_moveCount")
    private int moveCount;

    @Column(name = "game_lastMoveData")
    private String lastMoveData;

    @Column(name = "game_board", columnDefinition = "TEXT", nullable = true)
    private String board;

    @Column(name = "game_isBlackPlayerCastled")
    private boolean isBlackPlayerCastled;

    @Column(name = "game_isWhitePlayerCastled")
    private boolean isWhitePlayerCastled;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_isPlayerTurn", nullable = false)
    private Alliance isPlayerTurn = Alliance.WHITE;

    public enum GameStatus {
        WAITING_FOR_OPPONENT,
        IN_PROGRESS,
        COMPLETED,
        ABANDONED,
        ACTIVE,
        CHECKMATE,
        DRAW,
    }

    public enum DrawType {
        STALEMATE("Draw by stalemate"),
        THREEFOLD_REPETITION("Draw by threefold repetition"),
        FIFTY_MOVE_RULE("Draw by fifty move rule"),
        INSUFFICIENT_MATERIAL("Draw by insufficient material"),
        MUTUAL_AGREEMENT("Draw by mutual agreement");

        private final String description;

        DrawType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
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