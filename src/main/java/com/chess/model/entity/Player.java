package com.chess.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.chess.model.entity.Game.GameStatus;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50) // nullable = false Creates a database-level constraint (NOT NULL), Throws SQL exception if you try to insert null
    private String username;

    @NotNull
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Min(value = 0, message = "Rating cannot be negative")
    @Max(value = 3000, message = "Rating cannot exceed 3000")
    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed;

    @Column(name = "games_won")
    private int gamesWon;

    @Column(name = "games_lost")
    private int gamesLost;

    @Column(name = "games_draw")
    private int gamesDraw;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "whitePlayer")
    private List<Game> gamesAsWhite = new ArrayList<>();

    @OneToMany(mappedBy = "blackPlayer")
    private List<Game> gamesAsBlack = new ArrayList<>();

    @Builder
    public Player(String username, String email) {
        this.username = username;
        this.email = email;
        this.rating = 800;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();
    }

    public List<Game> getAllGames() {
        List<Game> allGames = new ArrayList<>();
        allGames.addAll(gamesAsWhite);
        allGames.addAll(gamesAsBlack);
        return allGames;
    }

    public List<Game> getActiveGames() {
        List<Game> activeGames = new ArrayList<>();
        activeGames.addAll(gamesAsWhite.stream()
            .filter(game -> game.getStatus() == GameStatus.ACTIVE)
            .collect(Collectors.toList()));
        activeGames.addAll(gamesAsBlack.stream()
            .filter(game -> game.getStatus() == GameStatus.ACTIVE)
            .collect(Collectors.toList()));
        return activeGames;
    }
} 