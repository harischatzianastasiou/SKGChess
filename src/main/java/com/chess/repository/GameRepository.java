package com.chess.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.Game;
import com.chess.model.entity.Game.GameStatus;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByWhitePlayerIdOrBlackPlayerId(String userId, String userId2);
}

