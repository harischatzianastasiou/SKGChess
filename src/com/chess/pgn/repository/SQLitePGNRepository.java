package com.chess.pgn.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.chess.db.DatabaseManager;
import com.chess.pgn.model.PGNGame;
import com.chess.pgn.repository.PGNRepository;

public class SQLitePGNRepository implements PGNRepository {
    private final DatabaseManager dbManager;
    
    public SQLitePGNRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    @Override
    public void save(PGNGame game) {
        String sql = """
            INSERT INTO pgn_games (event, site, date, round, white, black, result, moves, eco)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, game.getEvent());
            pstmt.setString(2, game.getSite());
            pstmt.setDate(3, game.getDate());
            pstmt.setString(4, game.getRound());
            pstmt.setString(5, game.getWhite());
            pstmt.setString(6, game.getBlack());
            pstmt.setString(7, game.getResult());
            pstmt.setString(8, game.getMoves());
            pstmt.setString(9, game.getEco());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving game to database", e);
        }
    }
    
    @Override
    public Optional<PGNGame> findById(long id) {
        String sql = "SELECT * FROM pgn_games WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving game from database", e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<PGNGame> findAll() {
        List<PGNGame> games = new ArrayList<>();
        String sql = "SELECT * FROM pgn_games ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                games.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving games from database", e);
        }
        
        return games;
    }
    
    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM pgn_games WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting game from database", e);
        }
    }
    
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM pgn_games";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting games from database", e);
        }
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM pgn_games";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error counting games in database", e);
        }
    }
    
    private PGNGame mapResultSetToGame(ResultSet rs) throws SQLException {
        PGNGame game = new PGNGame(
            rs.getString("event"),
            rs.getString("site"),
            rs.getDate("date"),
            rs.getString("round"),
            rs.getString("white"),
            rs.getString("black"),
            rs.getString("result"),
            rs.getString("moves"),
            rs.getString("eco")
        );
        game.setId(rs.getLong("id"));
        return game;
    }
} 