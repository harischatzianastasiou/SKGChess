package com.chess.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

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
    @JoinColumn(name = "white_player_id", nullable = true)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "black_player_id", nullable = true)
    private User blackPlayer;

    @Column(name = "fen_position", columnDefinition = "TEXT")
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

    @Builder // Lombok builder pattern
    public Game(User user) {
        this.whitePlayer = user;
        this.fenPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        this.status = GameStatus.WAITING_FOR_OPPONENT;
    }
} 