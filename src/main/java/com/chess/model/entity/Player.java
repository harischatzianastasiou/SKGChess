package com.chess.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "players")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotNull
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    @JsonIgnore //Prevents the hashed password from being serialized and sent in the JSON response. Will use later also json ignore in the getter so that serialization is not affected. But use jsonproperty in the setter to enable the deserialization when user sends the json.
    private String password;

    @Min(value = 0, message = "Rating cannot be negative")
    @Max(value = 3000, message = "Rating cannot exceed 3000")
    @Column(name = "rating", nullable = false)
    private int rating = 800;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;

    @Column(name = "games_won")
    private int gamesWon = 0;

    @Column(name = "games_lost")
    private int gamesLost = 0;

    @Column(name = "games_draw")
    private int gamesDraw = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin = LocalDateTime.now();

    @JsonIgnore //Prevents the field from being serialized and sent in the JSON response
    @OneToMany(mappedBy = "whitePlayer")
    private List<Game> gamesAsWhite = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "blackPlayer")
    private List<Game> gamesAsBlack = new ArrayList<>();

    //Won't be called for updates (that would use @PreUpdate instead)
    @PrePersist//Called by JPA/Hibernate automatically//This annotation is used to specify that the method should be called when the entity is persisted to the database. It captures the exact database insertion time.
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLogin = LocalDateTime.now();
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(final String password) {
        this.password = password;
    }
    @Builder
    public Player(String username, String email) {
        this.username = username;
        this.email = email;
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