package com.chess.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chess.model.entity.User;
import com.chess.model.entity.Game;
import com.chess.repository.UserRepository;
import com.chess.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CleanupService {
    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public CleanupService(UserRepository userRepository, GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Scheduled task to clean up guest users and their games
     * Runs every day at 12:56
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight (00:00) every day
    @Transactional
    public void cleanupGuestUsers() {
        logger.info("Starting cleanup of guest users and their games");
        LocalDateTime now = LocalDateTime.now();
        
        // Find all guest users
        List<User> guestUsers = userRepository.findAll().stream()
            .filter(user -> user.getUsername().startsWith("guest_"))
            .toList();
        
        for (User guestUser : guestUsers) {
            try {
                // Find all games associated with this guest user
                List<Game> userGames = gameRepository.findByWhitePlayerIdOrBlackPlayerId(
                    guestUser.getId(), guestUser.getId());
                
                // Delete all games
                for (Game game : userGames) {
                    gameRepository.delete(game);
                    logger.info("Deleted game {} for guest user {}", game.getId(), guestUser.getUsername());
                }
                
                // Delete the guest user
                userRepository.delete(guestUser);
                logger.info("Deleted guest user {}", guestUser.getUsername());
            } catch (Exception e) {
                logger.error("Error cleaning up guest user {}: {}", guestUser.getUsername(), e.getMessage());
            }
        }
        
        logger.info("Finished cleanup of guest users and their games");
    }
} 