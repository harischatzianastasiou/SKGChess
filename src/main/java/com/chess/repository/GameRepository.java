package com.chess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    List<Game> findByStatus(String status);
    List<Game> findByWhitePlayerIdOrBlackPlayerId(String userId, String userId2);

    /**
     * Find the oldest game with WAITING_FOR_OPPONENT status
     * @return The oldest waiting game, or null if none found
     */
    @Query("SELECT g FROM Game g WHERE g.status = 'WAITING_FOR_OPPONENT' ORDER BY g.createdAt ASC LIMIT 1")
    Optional<Game> findOldestWaitingGame();

    /**
     * Find games for a user sorted by creation date in descending order
     * @param userId The user's ID
     * @param userId2 The same user's ID (for white or black player)
     * @return List of games sorted by creation date descending
     */
    @Query("SELECT g FROM Game g WHERE g.whitePlayer.id = ?1 OR g.blackPlayer.id = ?2 ORDER BY g.createdAt DESC")
    List<Game> findLastGamesByWhitePlayerIdOrBlackPlayerId(String userId, String userId2);
}

