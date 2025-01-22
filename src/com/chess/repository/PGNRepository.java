package com.chess.repository;

import java.util.List;
import java.util.Optional;

import com.chess.infrastructure.db.PGNGame;

public interface PGNRepository {
    void save(PGNGame game);
    Optional<PGNGame> findById(long id);
    List<PGNGame> findAll();
    void deleteById(long id);
    void deleteAll();
    long count();
} 