package com.chess.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.Invitation;
import com.chess.model.entity.User;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String> {
    
    /**
     * Find all pending invitations for a specific user (as invitee)
     * @param invitee The user who received the invitations
     * @return List of pending invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.invitee = :invitee AND i.status = 'PENDING' AND i.expiresAt > :now ORDER BY i.createdAt DESC")
    List<Invitation> findPendingInvitationsForUser(@Param("invitee") User invitee, @Param("now") LocalDateTime now);
    
    /**
     * Find all invitations sent by a specific user (as inviter)
     * @param inviter The user who sent the invitations
     * @return List of invitations sent by the user
     */
    @Query("SELECT i FROM Invitation i WHERE i.inviter = :inviter ORDER BY i.createdAt DESC")
    List<Invitation> findInvitationsSentByUser(@Param("inviter") User inviter);
    
    /**
     * Find all invitations received by a specific user (as invitee)
     * @param invitee The user who received the invitations
     * @return List of invitations received by the user
     */
    @Query("SELECT i FROM Invitation i WHERE i.invitee = :invitee ORDER BY i.createdAt DESC")
    List<Invitation> findInvitationsReceivedByUser(@Param("invitee") User invitee);
    
    /**
     * Check if there's already a pending invitation between two users
     * @param inviter The user who would send the invitation
     * @param invitee The user who would receive the invitation
     * @return true if there's already a pending invitation
     */
    @Query("SELECT COUNT(i) > 0 FROM Invitation i WHERE i.inviter = :inviter AND i.invitee = :invitee AND i.status = 'PENDING' AND i.expiresAt > :now")
    boolean existsPendingInvitationBetweenUsers(@Param("inviter") User inviter, @Param("invitee") User invitee, @Param("now") LocalDateTime now);
    
    /**
     * Find expired invitations and mark them as expired
     * @param now Current time
     * @return List of expired invitations
     */
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' AND i.expiresAt <= :now")
    List<Invitation> findExpiredInvitations(@Param("now") LocalDateTime now);
} 