package com.chess.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    
    List<Game> findByStatus(String status);
    
    List<Game> findByWhitePlayerIdOrBlackPlayerId(String userId, String userId2);


    @Query(value = "SELECT g FROM Game g WHERE (g.whitePlayer.id = ?1 OR g.blackPlayer.id = ?2) AND g.status = 'CHECKMATE' ORDER BY g.createdAt DESC")
    List<Game> findLast6CheckmateGamesByWhitePlayerIdOrBlackPlayerId(String playerId1, String playerId2, Pageable pageable);

    @Query(value = "SELECT g FROM Game g WHERE (g.whitePlayer.id = ?1 OR g.blackPlayer.id = ?2) AND g.status != 'WAITING_FOR_OPPONENT' ORDER BY g.createdAt DESC")
    List<Game> findLast6GamesByWhitePlayerIdOrBlackPlayerId(String playerId1, String playerId2, Pageable pageable);

    @Query(value = "SELECT g FROM Game g WHERE g.whitePlayer.id = ?1 OR g.blackPlayer.id = ?2 ORDER BY g.createdAt DESC")
    List<Game> findLastGameByWhitePlayerIdOrBlackPlayerId(String playerId1, String playerId2, Pageable pageable);

    @Query(value = "SELECT COUNT(g) FROM Game g WHERE (g.whitePlayer.id = ?1 OR g.blackPlayer.id = ?2) AND g.status != 'WAITING_FOR_OPPONENT'")
    int numOfUserGames(String playerId1, String playerId2);

}

