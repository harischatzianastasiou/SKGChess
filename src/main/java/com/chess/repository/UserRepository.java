package com.chess.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.model.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Query("SELECT p FROM User p WHERE p.username = :identifier OR p.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
}
