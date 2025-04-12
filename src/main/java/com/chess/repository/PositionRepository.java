package com.chess.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chess.model.entity.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
    
}
