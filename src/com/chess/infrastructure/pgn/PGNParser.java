package com.chess.infrastructure.pgn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chess.infrastructure.db.PGNGame;
import com.chess.infrastructure.db.exception.DatabaseException;

public class PGNParser {
    private static final Pattern TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"(.+?)\"\\]");
    private static final Pattern MOVE_NUMBER_PATTERN = Pattern.compile("\\d+\\.\\s*");
    
    public static List<PGNGame> parsePGNFile(String filePath) {
        List<PGNGame> games = new ArrayList<>();
        StringBuilder currentGame = new StringBuilder();
        boolean inGame = false;
        int gamesProcessed = 0;
        long startTime = System.currentTimeMillis();
        
        System.out.println("Starting to parse PGN file: " + filePath);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Start of a new game
                if (line.startsWith("[") && !line.trim().isEmpty()) {
                    if (inGame && currentGame.length() > 0) {
                        // Process previous game
                        PGNGame game = parseGame(currentGame.toString());
                        if (game != null) {
                            games.add(game);
                            gamesProcessed++;
                            if (gamesProcessed % 1000 == 0) {
                                long currentTime = System.currentTimeMillis();
                                double timeInSeconds = (currentTime - startTime) / 1000.0;
                                double gamesPerSecond = gamesProcessed / timeInSeconds;
                                System.out.printf("Processed %,d games (%.1f games/sec)%n", 
                                    gamesProcessed, gamesPerSecond);
                            }
                        }
                        currentGame = new StringBuilder();
                    }
                    inGame = true;
                }
                
                if (!line.isEmpty()) {
                    currentGame.append(line).append("\n");
                }
            }
            
            // Process the last game
            if (inGame && currentGame.length() > 0) {
                PGNGame game = parseGame(currentGame.toString());
                if (game != null) {
                    games.add(game);
                    gamesProcessed++;
                }
            }
            
            long endTime = System.currentTimeMillis();
            double totalTimeInSeconds = (endTime - startTime) / 1000.0;
            double finalGamesPerSecond = gamesProcessed / totalTimeInSeconds;
            
            System.out.println("\nPGN parsing completed:");
            System.out.printf("Total games processed: %,d%n", gamesProcessed);
            System.out.printf("Total time: %.1f seconds%n", totalTimeInSeconds);
            System.out.printf("Average speed: %.1f games/second%n", finalGamesPerSecond);
            System.out.println("Starting database import...");
            
        } catch (IOException e) {
            throw new DatabaseException("Error reading PGN file: " + e.getMessage(), e);
        }
        
        return games;
    }
    
    private static PGNGame parseGame(String gameText) {
        Map<String, String> tags = new HashMap<>();
        StringBuilder moves = new StringBuilder();
        boolean inMoves = false;
        
        String[] lines = gameText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[")) {
                Matcher matcher = TAG_PATTERN.matcher(line);
                if (matcher.find()) {
                    tags.put(matcher.group(1), matcher.group(2));
                }
            } else if (!line.isEmpty()) {
                inMoves = true;
                // Clean up move text: remove move numbers and normalize spaces
                line = MOVE_NUMBER_PATTERN.matcher(line).replaceAll("");
                moves.append(line.replaceAll("\\s+", " ")).append(" ");
            }
        }
        
        // Parse date with better error handling
        Date date = null;
        try {
            String dateStr = tags.getOrDefault("Date", "1970.01.01")
                               .replaceAll("[?]", "0")
                               .replaceAll("\\.", "-");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = new Date(sdf.parse(dateStr).getTime());
        } catch (ParseException e) {
            System.err.println("Error parsing date, using default: " + e.getMessage());
            date = new Date(0); // Unix epoch as default
        }
        
        // Only create game if we have both metadata and moves
        if (!tags.isEmpty() && moves.length() > 0) {
            return new PGNGame(
                tags.getOrDefault("Event", "Unknown Event"),
                tags.getOrDefault("Site", "Unknown Site"),
                date,
                tags.getOrDefault("Round", "1"),
                tags.getOrDefault("White", "Unknown White"),
                tags.getOrDefault("Black", "Unknown Black"),
                tags.getOrDefault("Result", "*"),
                moves.toString().trim(),
                tags.getOrDefault("ECO", "")
            );
        }
        
        return null;
    }
} 