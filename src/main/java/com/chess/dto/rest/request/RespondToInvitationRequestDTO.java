package com.chess.dto.rest.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondToInvitationRequestDTO {
    // Required field - username of the person responding to the invitation
    @NotBlank(message = "Username is required")
    private String username;
    
    // Required field - ID of the invitation to respond to
    @NotBlank(message = "Invitation ID is required")
    private String invitationId;
    
    // Required field - response action (accept or decline)
    @NotBlank(message = "Response action is required")
    private String action; // "accept" or "decline"
} 