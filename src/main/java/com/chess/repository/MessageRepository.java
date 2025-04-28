package com.chess.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chess.model.entity.Message;
import com.chess.model.entity.User;

/**
 * Repository interface for Message entity.
 * Provides methods to interact with the messages table in the database.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    /**
     * Find all messages for a specific user
     * @param user The user to find messages for
     * @return List of messages for the user
     */
    List<Message> findByUserOrderByTimestampDesc(User user);
    
    /**
     * Find all unread messages for a specific user
     * @param user The user to find unread messages for
     * @return List of unread messages for the user
     */
    List<Message> findByUserAndReadFalseOrderByTimestampDesc(User user);
    
    /**
     * Mark all messages for a user as read
     * @param user The user whose messages should be marked as read
     */
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.user = :user AND m.read = false")
    void markAllAsRead(@Param("user") User user);
    
    /**
     * Delete all messages for a specific game
     * @param gameId The ID of the game whose messages should be deleted
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.gameId = :gameId")
    void deleteByGameId(@Param("gameId") String gameId);
} 