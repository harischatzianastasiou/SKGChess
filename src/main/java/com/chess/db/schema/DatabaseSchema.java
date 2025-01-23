package com.chess.db.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSchema {
    
    private static final List<String> SCHEMA_STATEMENTS = new ArrayList<>();
    
    static {
        // Initialize schema statements
        SCHEMA_STATEMENTS.add(createPgnGamesTable());
        // Add more table creation statements here as needed
    }
    
    private static String createPgnGamesTable() {
        return """
            CREATE TABLE IF NOT EXISTS pgn_games (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event TEXT,
                site TEXT,
                date DATE,
                round TEXT,
                white TEXT,
                black TEXT,
                result TEXT,
                moves TEXT,
                eco TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
    }
    
    public static void createSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Execute all schema statements
            for (String sql : SCHEMA_STATEMENTS) {
                stmt.execute(sql);
            }
        }
    }
    
    public static void dropSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS pgn_games");
            // Add more table drops here as needed
        }
    }
} 