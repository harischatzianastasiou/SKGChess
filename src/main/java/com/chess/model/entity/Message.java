package com.chess.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

/**
 * Entity class representing a game message.
 * This will store messages about game events that need to be persistent.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "messages")
public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "opponent_username", nullable = false)
    private String opponentUsername;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "read_status", nullable = false)
    private boolean read = false;

    @Column(name = "message_type", nullable = false)
    private String type = "GAME_STARTED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Constructor for creating a new message
     */
    public Message(String gameId, String opponentUsername, String content, User user) {
        this.gameId = gameId;
        this.opponentUsername = opponentUsername;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.user = user;
    }

    /**
     * Constructor for creating a new message with type
     */
    public Message(String gameId, String opponentUsername, String content, String type, User user) {
        this.gameId = gameId;
        this.opponentUsername = opponentUsername;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.user = user;
    }
} 