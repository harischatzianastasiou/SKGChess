package com.chess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.GamePosition;

/**
 * Repository interface for GamePosition entity
 * Handles database operations for storing and retrieving game positions
 */
@Repository
public interface GamePositionRepository extends JpaRepository<GamePosition, String> {
    
    /**
     * Find all positions for a specific game, ordered by move number
     * @param gameId The ID of the game
     * @return List of positions ordered by move number (ascending)
     */
    @Query("SELECT gp FROM GamePosition gp WHERE gp.game.id = :gameId ORDER BY gp.moveNumber ASC")
    List<GamePosition> findByGameIdOrderByMoveNumberAsc(@Param("gameId") String gameId);
    
    /**
     * Find a specific position by game ID and move number
     * @param gameId The ID of the game
     * @param moveNumber The move number to find
     * @return Optional containing the position if found
     */
    @Query("SELECT gp FROM GamePosition gp WHERE gp.game.id = :gameId AND gp.moveNumber = :moveNumber")
    Optional<GamePosition> findByGameIdAndMoveNumber(@Param("gameId") String gameId, @Param("moveNumber") int moveNumber);
    
    /**
     * Find the latest position for a game (highest move number)
     * @param gameId The ID of the game
     * @return Optional containing the latest position if found
     */
    @Query("SELECT gp FROM GamePosition gp WHERE gp.game.id = :gameId ORDER BY gp.moveNumber DESC LIMIT 1")
    Optional<GamePosition> findLatestPositionByGameId(@Param("gameId") String gameId);
    
    /**
     * Count total positions for a game
     * @param gameId The ID of the game
     * @return Number of positions stored for the game
     */
    @Query("SELECT COUNT(gp) FROM GamePosition gp WHERE gp.game.id = :gameId")
    long countByGameId(@Param("gameId") String gameId);
    
    /**
     * Delete all positions for a specific game
     * @param gameId The ID of the game
     */
    @Query("DELETE FROM GamePosition gp WHERE gp.game.id = :gameId")
    void deleteByGameId(@Param("gameId") String gameId);
} 