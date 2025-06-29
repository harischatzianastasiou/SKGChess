package com.chess.model.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

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
@Table(name = "INVITATION")
public class Invitation implements Serializable {
    // Serializable is used to convert the object to a byte stream, so it can be sent over the network
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "S_ID")
    private String id;

    // Invitation details
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "S_INVITER_ID", nullable = false)
    private User inviter; // User who sent the invitation

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "S_INVITEE_ID", nullable = false)
    private User invitee; // User who received the invitation

    @Column(name = "N_TIMECONTROLMINUTES", nullable = false)
    private Integer timeControlMinutes; // Time control for the game

    @Column(name = "S_PLAYERCOLOR", nullable = true)
    private String playerColor; // Color preference (white, black, random)

    @Column(name = "D_CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "D_EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt; // When the invitation expires

    @Enumerated(EnumType.STRING)
    @Column(name = "S_STATUS", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "S_GAME_ID", nullable = true)
    private String gameId; // Game ID if invitation was accepted and game was created

    @Column(name = "N_INCREMENTSECONDS", nullable = true)
    private Integer incrementSeconds; // Increment per move in seconds

    public enum InvitationStatus {
        PENDING,    // Invitation is waiting for response
        ACCEPTED,   // Invitation was accepted and game was created
        DECLINED,   // Invitation was declined
        EXPIRED,    // Invitation expired
        CANCELLED   // Invitation was cancelled by inviter
    }

    // Constructor to set expiration time (24 hours from creation)
    public Invitation(User inviter, User invitee, Integer timeControlMinutes, Integer incrementSeconds, String playerColor) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.timeControlMinutes = timeControlMinutes;
        this.incrementSeconds = incrementSeconds;
        this.playerColor = playerColor;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24); // Expires in 24 hours
        this.status = InvitationStatus.PENDING;
    }
} 