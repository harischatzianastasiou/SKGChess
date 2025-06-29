package com.chess.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.dto.rest.request.CreateInvitationRequestDTO;
import com.chess.dto.rest.request.RespondToInvitationRequestDTO;
import com.chess.dto.rest.response.ErrorResponseDTO;
import com.chess.dto.rest.response.InvitationDTO;
import com.chess.model.entity.Invitation;
import com.chess.service.InvitationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = {"https://skgchess.com", "https://www.skgchess.com", "https://skgchess.fly.dev", "http://localhost:8080"}, maxAge = 3600)
public class InvitationController {
    
    private final InvitationService invitationService;
    
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }
    
    /**
     * Create a new game invitation
     * @param request The invitation request containing inviter, opponent, time control, and color preference
     * @return The created invitation
     */
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createInvitation(@RequestBody @Valid CreateInvitationRequestDTO request) {
        try {
            log.info("Creating invitation from {} to {} for {} minute game", 
                    request.getUsername(), request.getOpponentUsername(), request.getTimeControlMinutes());
            
            // Create the invitation
            Invitation invitation = invitationService.createInvitation(
                request.getUsername(),
                request.getOpponentUsername(),
                request.getTimeControlMinutes(),
                request.getIncrementSeconds(),
                request.getPlayerColor()
            );
            
            // Convert to DTO and return
            InvitationDTO invitationDTO = InvitationDTO.fromInvitation(invitation);
            return ResponseEntity.ok(invitationDTO);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid invitation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating invitation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to create invitation"));
        }
    }
    
    /**
     * Respond to an invitation (accept or decline)
     * @param request The response request containing username, invitation ID, and action
     * @return The updated invitation
     */
    @PostMapping(value = "/respond", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> respondToInvitation(@RequestBody @Valid RespondToInvitationRequestDTO request) {
        try {
            log.info("User {} responding to invitation {} with action: {}", 
                    request.getUsername(), request.getInvitationId(), request.getAction());
            
            // Respond to the invitation
            Invitation invitation = invitationService.respondToInvitation(
                request.getUsername(),
                request.getInvitationId(),
                request.getAction()
            );
            
            // Convert to DTO and return
            InvitationDTO invitationDTO = InvitationDTO.fromInvitation(invitation);
            
            // If invitation was accepted, include the gameId in the response
            if ("accept".equalsIgnoreCase(request.getAction()) && invitation.getGameId() != null) {
                log.info("Invitation accepted, returning gameId: {}", invitation.getGameId());
            }
            
            return ResponseEntity.ok(invitationDTO);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid invitation response: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error responding to invitation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to respond to invitation"));
        }
    }
    
    /**
     * Get all pending invitations for a user
     * @param username The username to get invitations for
     * @return List of pending invitations
     */
    @GetMapping(value = "/pending/{username}", produces = "application/json")
    public ResponseEntity<?> getPendingInvitations(@PathVariable String username) {
        try {
            log.info("Getting pending invitations for user: {}", username);
            
            List<InvitationDTO> invitations = invitationService.getPendingInvitations(username);
            return ResponseEntity.ok(invitations);
            
        } catch (Exception e) {
            log.error("Error getting pending invitations for {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to get pending invitations"));
        }
    }
    
    /**
     * Get all invitations sent by a user
     * @param username The username to get sent invitations for
     * @return List of sent invitations
     */
    @GetMapping(value = "/sent/{username}", produces = "application/json")
    public ResponseEntity<?> getSentInvitations(@PathVariable String username) {
        try {
            log.info("Getting sent invitations for user: {}", username);
            
            List<InvitationDTO> invitations = invitationService.getSentInvitations(username);
            return ResponseEntity.ok(invitations);
            
        } catch (Exception e) {
            log.error("Error getting sent invitations for {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to get sent invitations"));
        }
    }
    
    /**
     * Get all invitations received by a user
     * @param username The username to get received invitations for
     * @return List of received invitations
     */
    @GetMapping(value = "/received/{username}", produces = "application/json")
    public ResponseEntity<?> getReceivedInvitations(@PathVariable String username) {
        try {
            log.info("Getting received invitations for user: {}", username);
            
            List<InvitationDTO> invitations = invitationService.getReceivedInvitations(username);
            return ResponseEntity.ok(invitations);
            
        } catch (Exception e) {
            log.error("Error getting received invitations for {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to get received invitations"));
        }
    }
    
    /**
     * Cancel an invitation (only the inviter can cancel)
     * @param username The username of the person cancelling
     * @param invitationId The ID of the invitation to cancel
     * @return The updated invitation
     */
    @PostMapping(value = "/cancel/{username}/{invitationId}", produces = "application/json")
    public ResponseEntity<?> cancelInvitation(@PathVariable String username, @PathVariable String invitationId) {
        try {
            log.info("User {} cancelling invitation: {}", username, invitationId);
            
            Invitation invitation = invitationService.cancelInvitation(username, invitationId);
            InvitationDTO invitationDTO = InvitationDTO.fromInvitation(invitation);
            return ResponseEntity.ok(invitationDTO);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid invitation cancellation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling invitation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to cancel invitation"));
        }
    }
    
    /**
     * Delete an accepted invitation (called after both players have joined the game)
     * @param invitationId The ID of the invitation to delete
     * @return Success response
     */
    @DeleteMapping(value = "/{invitationId}", produces = "application/json")
    public ResponseEntity<?> deleteAcceptedInvitation(@PathVariable String invitationId) {
        try {
            log.info("Deleting accepted invitation: {}", invitationId);
            
            invitationService.deleteAcceptedInvitation(invitationId);
            
            return ResponseEntity.ok().body("{\"message\":\"Invitation deleted successfully\"}");
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid invitation deletion request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting invitation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to delete invitation"));
        }
    }
} 