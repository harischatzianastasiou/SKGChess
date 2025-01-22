package com.chess.service;

import com.chess.infrastructure.db.PGNGame;
import com.chess.repository.PGNRepository;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class PGNService {
    private final PGNRepository pgnRepository;
    
    public PGNService(PGNRepository pgnRepository) {
        this.pgnRepository = pgnRepository;
    }
    
    public void saveGame(PGNGame game) {
        pgnRepository.save(game);
    }
    
    public void saveGames(List<PGNGame> games) {
        System.out.println("\nStarting database import of " + games.size() + " games...");
        long startTime = System.currentTimeMillis();
        int gamesImported = 0;
        
        for (PGNGame game : games) {
            saveGame(game);
            gamesImported++;
            
            if (gamesImported % 1000 == 0) {
                long currentTime = System.currentTimeMillis();
                double timeInSeconds = (currentTime - startTime) / 1000.0;
                double gamesPerSecond = gamesImported / timeInSeconds;
                System.out.printf("Imported %,d/%,d games (%.1f games/sec)%n", 
                    gamesImported, games.size(), gamesPerSecond);
            }
        }
        
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;
        double finalGamesPerSecond = gamesImported / totalTimeInSeconds;
        
        System.out.println("\nDatabase import completed:");
        System.out.printf("Total games imported: %,d%n", gamesImported);
        System.out.printf("Total time: %.1f seconds%n", totalTimeInSeconds);
        System.out.printf("Average speed: %.1f games/second%n", finalGamesPerSecond);
        System.out.println("Opening book is ready to use!");
    }
    
    public Optional<PGNGame> getGameById(long id) {
        return pgnRepository.findById(id);
    }
    
    public List<PGNGame> getAllGames() {
        return pgnRepository.findAll();
    }
    
    public void deleteGame(long id) {
        pgnRepository.deleteById(id);
    }
    
    public void deleteAllGames() {
        pgnRepository.deleteAll();
    }
    
    public long getGameCount() {
        return pgnRepository.count();
    }
    
    public DatabaseInfo getDatabaseInfo() {
        File dbFile = new File(System.getProperty("user.dir") + File.separator + "chess_games.db");
        return new DatabaseInfo(
            dbFile.getAbsolutePath(),
            getGameCount(),
            dbFile.length()
        );
    }
    
    public static class DatabaseInfo {
        private final String location;
        private final long gameCount;
        private final long sizeInBytes;
        
        public DatabaseInfo(String location, long gameCount, long sizeInBytes) {
            this.location = location;
            this.gameCount = gameCount;
            this.sizeInBytes = sizeInBytes;
        }
        
        public String getLocation() { return location; }
        public long getGameCount() { return gameCount; }
        public long getSizeInBytes() { return sizeInBytes; }
        public double getSizeInMB() { return sizeInBytes / (1024.0 * 1024.0); }
        
        @Override
        public String toString() {
            return String.format("""
                Database Location: %s
                Total Games: %d
                Database Size: %.2f MB
                """,
                location,
                gameCount,
                getSizeInMB());
        }
    }
} 