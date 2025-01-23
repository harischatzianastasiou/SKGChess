package com.chess.db;

import com.chess.db.config.DatabaseConfig;
import com.chess.db.exception.DatabaseException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.chess.db.schema.DatabaseSchema;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        File dbFile = new File(DatabaseConfig.getDbPath());
        if (!dbFile.exists()) {
            createNewDatabase();
        }
        
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException.InitializationException(
                "SQLite JDBC driver not found", e);
        }
    }
    
    private void createNewDatabase() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseSchema.createSchema(conn);
                System.out.println("Created new database at: " + DatabaseConfig.getDbPath());
            }
        } catch (SQLException e) {
            throw new DatabaseException.InitializationException(
                "Failed to create database", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DatabaseConfig.getDbUrl());
            configureConnection(connection);
        }
        return connection;
    }
    
    private void configureConnection(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (DatabaseConfig.isForeignKeysEnabled()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            stmt.execute("PRAGMA journal_mode = " + DatabaseConfig.getJournalMode());
            stmt.execute("PRAGMA synchronous = " + DatabaseConfig.getSynchronousMode());
        }
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                throw new DatabaseException.ConnectionException(
                    "Failed to close database connection", e);
            }
        }
    }
} 