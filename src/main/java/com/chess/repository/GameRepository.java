package com.chess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByWhitePlayerIdOrBlackPlayerId(String userId, String userId2);

    /**
     * Find the oldest game with WAITING_FOR_OPPONENT status
     * @return The oldest waiting game, or null if none found
     */
    @Query("SELECT g FROM Game g WHERE g.status = 'WAITING_FOR_OPPONENT' ORDER BY g.createdAt ASC LIMIT 1")
    Optional<Game> findOldestWaitingGame();
}

