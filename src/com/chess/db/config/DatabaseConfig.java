package com.chess.db.config;

import java.io.File;

public class DatabaseConfig {
    private static final String DB_NAME = "chess_games.db";
    private static final String DB_PATH = System.getProperty("user.dir") + File.separator + DB_NAME;
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    
    // Database configuration properties
    private static final boolean FOREIGN_KEYS_ENABLED = true;
    private static final String JOURNAL_MODE = "WAL";
    private static final String SYNCHRONOUS_MODE = "NORMAL";
    
    private DatabaseConfig() {
        // Private constructor to prevent instantiation
    }
    
    public static String getDbUrl() {
        return DB_URL;
    }
    
    public static String getDbPath() {
        return DB_PATH;
    }
    
    public static String getDbName() {
        return DB_NAME;
    }
    
    public static boolean isForeignKeysEnabled() {
        return FOREIGN_KEYS_ENABLED;
    }
    
    public static String getJournalMode() {
        return JOURNAL_MODE;
    }
    
    public static String getSynchronousMode() {
        return SYNCHRONOUS_MODE;
    }
} 