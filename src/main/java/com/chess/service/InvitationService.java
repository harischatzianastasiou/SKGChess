package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chess.dto.rest.response.InvitationDTO;
import com.chess.exception.GameNotFoundException;
import com.chess.exception.UserNotFoundException;
import com.chess.model.entity.Game;
import com.chess.model.entity.Invitation;
import com.chess.model.entity.User;
import com.chess.repository.GameRepository;
import com.chess.repository.InvitationRepository;
import com.chess.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InvitationService {
    
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    
    public InvitationService(InvitationRepository invitationRepository, 
                           UserRepository userRepository, 
                           GameRepository gameRepository,
                           GameService gameService,
                           SimpMessagingTemplate messagingTemplate) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Create a new game invitation
     * @param inviterUsername Username of the person sending the invitation
     * @param opponentUsername Username of the person to invite
     * @param timeControlMinutes Time control for the game
     * @param incrementSeconds Increment seconds for the game
     * @param playerColor Color preference
     * @return The created invitation
     */
    @Transactional
    public Invitation createInvitation(String inviterUsername, String opponentUsername, 
                                     Integer timeControlMinutes, Integer incrementSeconds, String playerColor) {
        // Find the inviter (person sending the invitation)
        User inviter = userRepository.findByUsername(inviterUsername)
            .orElseThrow(() -> new UserNotFoundException(inviterUsername));
        
        // Find the invitee (person being invited)
        User invitee = userRepository.findByUsername(opponentUsername)
            .orElseThrow(() -> new UserNotFoundException(opponentUsername));
        
        // Check if users are trying to invite themselves
        if (inviter.getId().equals(invitee.getId())) {
            throw new IllegalArgumentException("You cannot invite yourself to a game");
        }
        
        // Check if either user already has an active game
        if (userRepository.existsActiveGameForUser(inviter.getId())) {
            throw new IllegalArgumentException("You already have an active game");
        }
        if (userRepository.existsActiveGameForUser(invitee.getId())) {
            throw new IllegalArgumentException("The opponent already has an active game");
        }
        
        // Check if there's already a pending invitation between these users
        if (invitationRepository.existsPendingInvitationBetweenUsers(inviter, invitee, LocalDateTime.now())) {
            throw new IllegalArgumentException("You already have a pending invitation with this user");
        }
        
        // Create the invitation
        Invitation invitation = new Invitation(inviter, invitee, timeControlMinutes, incrementSeconds, playerColor);
        invitation = invitationRepository.save(invitation);
        
        // Send WebSocket notification to the invitee
        sendInvitationNotification(invitation);
        
        log.info("Created invitation from {} to {} for {} minute game", 
                inviterUsername, opponentUsername, timeControlMinutes);
        
        return invitation;
    }
    
    /**
     * Respond to an invitation (accept or decline)
     * @param username Username of the person responding
     * @param invitationId ID of the invitation
     * @param action "accept" or "decline"
     * @return The updated invitation
     */
    @Transactional
    public Invitation respondToInvitation(String username, String invitationId, String action) {
        // Find the invitation
        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        
        // Verify the user is the invitee
        if (!invitation.getInvitee().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only respond to invitations sent to you");
        }
        
        // Check if invitation is still pending and not expired
        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("This invitation is no longer pending");
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("This invitation has expired");
        }
        
        if ("accept".equalsIgnoreCase(action)) {
            return acceptInvitation(invitation);
        } else if ("decline".equalsIgnoreCase(action)) {
            declineInvitation(invitation);
            // Return the original invitation for the response (it will be null in the database)
            return invitation;
        } else {
            throw new IllegalArgumentException("Invalid action. Use 'accept' or 'decline'");
        }
    }
    
    /**
     * Accept an invitation and create a game
     * @param invitation The invitation to accept
     * @return The updated invitation (before deletion)
     */
    private Invitation acceptInvitation(Invitation invitation) {
        try {
            log.info("Starting invitation acceptance process for invitation: {}", invitation.getId());
            log.info("Inviter: {}, Invitee: {}, Time Control: {} minutes", 
                    invitation.getInviter().getUsername(), 
                    invitation.getInvitee().getUsername(), 
                    invitation.getTimeControlMinutes());
            
            // Create a game with the invitation details
            log.info("Creating game for invitation...");
            Game game = gameService.createGame(
                invitation.getInviter().getUsername(),
                "standard",
                invitation.getTimeControlMinutes(),
                invitation.getIncrementSeconds(),
                false,
                null,
                invitation.getPlayerColor()
            );
            log.info("Game created successfully with ID: {}", game.getId());
            
            // Join the game with the invitee
            log.info("Joining invitee {} to game {}", invitation.getInvitee().getUsername(), game.getId());
            game = gameService.joinGame(game.getId(), invitation.getInvitee().getUsername());
            log.info("Invitee joined successfully. Game status: {}", game.getStatus());
            
            // Update invitation status and set game ID before deletion
            invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
            invitation.setGameId(game.getId());
            log.info("Invitation status updated to ACCEPTED with game ID: {}", game.getId());
            
            // Send WebSocket notification to both users
            log.info("Sending WebSocket notifications to both users...");
            sendGameCreatedNotification(invitation, game);
            log.info("WebSocket notifications sent successfully");
            
            // Delete the invitation immediately after acceptance
            log.info("Deleting invitation immediately after acceptance: {}", invitation.getId());
            invitationRepository.delete(invitation);
            log.info("Invitation deleted successfully");
            
            log.info("Invitation accepted, game created, and invitation deleted successfully: {}", game.getId());
            
            return invitation;
            
        } catch (Exception e) {
            log.error("Error accepting invitation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create game from invitation: " + e.getMessage());
        }
    }
    
    /**
     * Decline an invitation
     * @param invitation The invitation to decline
     * @return null since the invitation is deleted
     */
    private Invitation declineInvitation(Invitation invitation) {
        // Send WebSocket notification to the inviter before deleting
        sendInvitationDeclinedNotification(invitation);
        
        // Delete the invitation immediately instead of just marking it as declined
        invitationRepository.delete(invitation);
        
        log.info("Invitation declined and deleted by {}", invitation.getInvitee().getUsername());
        
        // Return null since the invitation no longer exists
        return null;
    }
    
    /**
     * Get all pending invitations for a user
     * @param username Username of the user
     * @return List of pending invitations
     */
    public List<InvitationDTO> getPendingInvitations(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        
        List<Invitation> invitations = invitationRepository.findPendingInvitationsForUser(user, LocalDateTime.now());
        
        return invitations.stream()
            .map(InvitationDTO::fromInvitation)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all invitations sent by a user
     * @param username Username of the user
     * @return List of sent invitations
     */
    public List<InvitationDTO> getSentInvitations(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        
        List<Invitation> invitations = invitationRepository.findInvitationsSentByUser(user);
        
        return invitations.stream()
            .map(InvitationDTO::fromInvitation)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all invitations received by a user
     * @param username Username of the user
     * @return List of received invitations
     */
    public List<InvitationDTO> getReceivedInvitations(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        
        List<Invitation> invitations = invitationRepository.findInvitationsReceivedByUser(user);
        
        return invitations.stream()
            .map(InvitationDTO::fromInvitation)
            .collect(Collectors.toList());
    }
    
    /**
     * Cancel an invitation (only the inviter can cancel)
     * @param username Username of the person cancelling
     * @param invitationId ID of the invitation
     * @return The updated invitation
     */
    @Transactional
    public Invitation cancelInvitation(String username, String invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        
        // Verify the user is the inviter
        if (!invitation.getInviter().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only cancel invitations you sent");
        }
        
        // Check if invitation is still pending
        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("This invitation is no longer pending");
        }
        
        invitation.setStatus(Invitation.InvitationStatus.CANCELLED);
        invitation = invitationRepository.save(invitation);
        
        // Send WebSocket notification to the invitee
        sendInvitationCancelledNotification(invitation);
        
        log.info("Invitation cancelled by {}", username);
        
        return invitation;
    }
    
    /**
     * Delete an accepted invitation (called after both players have joined the game)
     * @param invitationId ID of the invitation to delete
     */
    @Transactional
    public void deleteAcceptedInvitation(String invitationId) {
        try {
            log.info("=== DELETE INVITATION DEBUG ===");
            log.info("Attempting to delete invitation: {}", invitationId);
            
            Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
            
            log.info("Found invitation with status: {}", invitation.getStatus());
            log.info("✅ Deleting invitation: {}", invitationId);
            
            // Delete the invitation regardless of status since inviter is redirecting
            invitationRepository.delete(invitation);
            log.info("✅ Successfully deleted invitation: {}", invitationId);
            
        } catch (Exception e) {
            log.error("❌ Error deleting invitation {}: {}", invitationId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete invitation: " + e.getMessage());
        }
    }
    
    /**
     * Clean up expired invitations (called by scheduled task)
     */
    @Transactional
    public void cleanupExpiredInvitations() {
        List<Invitation> expiredInvitations = invitationRepository.findExpiredInvitations(LocalDateTime.now());
        
        for (Invitation invitation : expiredInvitations) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            
            // Send WebSocket notification to both users
            sendInvitationExpiredNotification(invitation);
        }
        
        if (!expiredInvitations.isEmpty()) {
            log.info("Marked {} invitations as expired", expiredInvitations.size());
        }
    }
    
    // WebSocket notification methods
    private void sendInvitationNotification(Invitation invitation) {
        String message = String.format(
            "{\"type\":\"INVITATION_RECEIVED\"," +
            "\"invitationId\":\"%s\"," +
            "\"inviterUsername\":\"%s\"," +
            "\"timeControlMinutes\":%d," +
            "\"playerColor\":\"%s\"," +
            "\"createdAt\":\"%s\"}",
            invitation.getId(),
            invitation.getInviter().getUsername(),
            invitation.getTimeControlMinutes(),
            invitation.getPlayerColor() != null ? invitation.getPlayerColor() : "random",
            invitation.getCreatedAt()
        );
        
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInvitee().getId(), message);
    }
    
    private void sendGameCreatedNotification(Invitation invitation, Game game) {
        String message = String.format(
            "{\"type\":\"INVITATION_ACCEPTED\"," +
            "\"invitationId\":\"%s\"," +
            "\"gameId\":\"%s\"," +
            "\"inviterUsername\":\"%s\"," +
            "\"inviteeUsername\":\"%s\"," +
            "\"shouldDeleteInvitation\":true}",
            invitation.getId(),
            game.getId(),
            invitation.getInviter().getUsername(),
            invitation.getInvitee().getUsername()
        );
        
        log.info("Sending WebSocket message to inviter (ID: {}): {}", invitation.getInviter().getId(), message);
        // Send to both users
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInviter().getId(), message);
        
        log.info("Sending WebSocket message to invitee (ID: {}): {}", invitation.getInvitee().getId(), message);
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInvitee().getId(), message);
        
        log.info("WebSocket notifications sent to both users for game: {}", game.getId());
    }
    
    private void sendInvitationDeclinedNotification(Invitation invitation) {
        String message = String.format(
            "{\"type\":\"INVITATION_DECLINED\"," +
            "\"invitationId\":\"%s\"," +
            "\"inviteeUsername\":\"%s\"}",
            invitation.getId(),
            invitation.getInvitee().getUsername()
        );
        
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInviter().getId(), message);
    }
    
    private void sendInvitationCancelledNotification(Invitation invitation) {
        String message = String.format(
            "{\"type\":\"INVITATION_CANCELLED\"," +
            "\"invitationId\":\"%s\"," +
            "\"inviterUsername\":\"%s\"}",
            invitation.getId(),
            invitation.getInviter().getUsername()
        );
        
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInvitee().getId(), message);
    }
    
    private void sendInvitationExpiredNotification(Invitation invitation) {
        String message = String.format(
            "{\"type\":\"INVITATION_EXPIRED\"," +
            "\"invitationId\":\"%s\"}",
            invitation.getId()
        );
        
        // Send to both users
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInviter().getId(), message);
        messagingTemplate.convertAndSend("/topic/user/" + invitation.getInvitee().getId(), message);
    }
} 