package com.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import com.chess.service.InvitationService;

@SpringBootApplication
@ComponentScan(basePackages = {"com.chess"})
@EnableScheduling
public class ChessWebApplication {
    
    @Autowired
    private InvitationService invitationService;
    
    public static void main(String[] args) {
        SpringApplication.run(ChessWebApplication.class, args);
    }
    
    /**
     * Clean up expired invitations every hour
     * This scheduled task runs every hour to mark expired invitations
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredInvitations() {
        try {
            invitationService.cleanupExpiredInvitations();
        } catch (Exception e) {
            // Log error but don't throw to prevent scheduled task from failing
            System.err.println("Error cleaning up expired invitations: " + e.getMessage());
        }
    }
}