package com.chess.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.model.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, String> {
    Optional<Player> findByUsername(String username);
    Optional<Player> findByEmail(String email);
    
    @Query("SELECT p FROM Player p WHERE p.username = :identifier OR p.email = :identifier")
    Optional<Player> findByUsernameOrEmail(@Param("identifier") String identifier);
}
