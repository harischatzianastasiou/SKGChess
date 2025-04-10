package com.chess.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "white_player_id", nullable = false)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "black_player_id", nullable = true)
    private User blackPlayer;

    @Column(name = "fen_position", columnDefinition = "TEXT", nullable = false)
    private String fenPosition;

    @Column(name = "pgn_moves", columnDefinition = "TEXT")
    private String pgnMoves = "";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus status = GameStatus.WAITING_FOR_OPPONENT;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "move_count")
    private int moveCount;

    @Column(name = "last_move_pgn")
    private String lastMovePgn;

    @Column(name = "is_black_player_castled")
    private boolean isBlackPlayerCastled;

    @Column(name = "is_white_player_castled")
    private boolean isWhitePlayerCastled;

    @Column(name = "game_type", nullable = true)
    private String gameType = "standard";

    @Column(name = "time_control_minutes", nullable = true)
    private Integer timeControlMinutes = 10;

    @Column(name = "is_rated", nullable = true)
    private Boolean isRated = true;

    @Column(name = "custom_rules", columnDefinition = "TEXT", nullable = true)
    private String customRules = "";

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
} 