package com.chess.dto.rest.response;

import java.time.LocalDateTime;
import com.chess.model.entity.Invitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Invitation entity
 * Used to transfer invitation data without exposing sensitive information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDTO {
    // Invitation ID
    private String id;
    
    // Inviter information
    private String inviterId;
    private String inviterUsername;
    
    // Invitee information
    private String inviteeId;
    private String inviteeUsername;
    
    // Game settings
    private Integer timeControlMinutes;
    private String playerColor;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // Status
    private String status;
    
    // Game ID (if accepted)
    private String gameId;
    
    /**
     * Convert an Invitation entity to an InvitationDTO
     * @param invitation The Invitation entity to convert
     * @return A new InvitationDTO with data from the Invitation entity
     */
    public static InvitationDTO fromInvitation(Invitation invitation) {
        return InvitationDTO.builder()
                .id(invitation.getId())
                .inviterId(invitation.getInviter().getId())
                .inviterUsername(invitation.getInviter().getUsername())
                .inviteeId(invitation.getInvitee().getId())
                .inviteeUsername(invitation.getInvitee().getUsername())
                .timeControlMinutes(invitation.getTimeControlMinutes())
                .playerColor(invitation.getPlayerColor())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .status(invitation.getStatus().name())
                .gameId(invitation.getGameId())
                .build();
    }
} 