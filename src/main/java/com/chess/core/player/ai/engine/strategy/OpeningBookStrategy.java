package com.chess.core.player.ai.engine.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.player.ai.OpeningBookCache;
import com.chess.core.player.ai.engine.utils.MoveConverter;
import com.chess.db.DatabaseManager;
import com.chess.pgn.model.PGNGame;
import com.chess.pgn.repository.SQLitePGNRepository;
import com.chess.pgn.service.PGNService;
import com.chess.service.game.GameService;

public class OpeningBookStrategy implements MoveStrategy {
    private final Map<String, List<String>> openingBook;
    private final MoveStrategy fallbackStrategy;
    private final Random random;
    private final PGNService pgnService;

    public OpeningBookStrategy(MoveStrategy fallbackStrategy) {
        System.out.println("\nInitializing opening book...");
        long startTime = System.currentTimeMillis();
        
        this.fallbackStrategy = fallbackStrategy;
        this.pgnService = new PGNService(new SQLitePGNRepository(DatabaseManager.getInstance()));
        
        // Try to load from cache first
        Map<String, List<String>> cachedBook = null;
        if (OpeningBookCache.isCacheValid()) {
            cachedBook = OpeningBookCache.loadFromFile();
        }
        
        if (cachedBook != null) {
            this.openingBook = cachedBook;
        } else {
            this.openingBook = initializeOpeningBook();
            // Save to cache for future use
            OpeningBookCache.saveToFile(this.openingBook);
        }
        
        this.random = new Random();
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Opening book ready! (%d positions loaded in %.1f seconds)%n", 
            openingBook.size(), (endTime - startTime) / 1000.0);
    }

    @Override
    public Move getBestMove(IBoard board) {
        String position = board.getFEN();
        String positionKey = getPositionKey(position);
        List<String> bookMoves = openingBook.get(positionKey);
        
        if (bookMoves != null && !bookMoves.isEmpty()) {
            String bookMove = bookMoves.get(random.nextInt(bookMoves.size()));
            Move move = MoveConverter.fromLongAlgebraic(bookMove, board);
            if (move != null) {
                return move;
            }
        }
        
        return fallbackStrategy.getBestMove(board);
    }

    @Override
    public String getName() {
        return "Opening Book + " + fallbackStrategy.getName();
    }

    private Map<String, List<String>> initializeOpeningBook() {
        Map<String, List<String>> book = new HashMap<>();
        
        // Load games from database
        System.out.println("Loading games from database...");
        List<PGNGame> games = pgnService.getAllGames();
        int totalGames = games.size();
        int gamesProcessed = 0;
        long startTime = System.currentTimeMillis();
        
        // Process each game
        for (PGNGame game : games) {
            String moves = game.getMoves();
            if (moves == null || moves.trim().isEmpty()) {
                continue;
            }
            
            try {
                // Start from initial position
                // Map<String, Game> gameMap = GameService.createNewGame();
                // String gameId = gameMap.keySet().iterator().next();
                IBoard currentBoard = GameService.getGame(GameService.getCurrentGameId()).getBoard();//add initial board :TODO
                String[] moveArray = moves.split("\\s+");
                
                // Process first 10 moves (20 plies) of each game
                int moveCount = 0;
                for (String moveStr : moveArray) {
                    if (moveCount >= 20) break; // Stop after 10 moves
                    
                    // Skip move numbers and annotations
                    if (moveStr.contains(".") || moveStr.contains("!") || moveStr.contains("?")) continue;
                    
                    try {
                        // Convert algebraic notation to long algebraic
                        Move move = MoveConverter.fromAlgebraic(moveStr, currentBoard);
                        if (move != null) {
                            // Add to opening book
                            String fen = currentBoard.getFEN();
                            String longAlgebraic = MoveConverter.toLongAlgebraic(move);
                            book.computeIfAbsent(getPositionKey(fen), k -> new ArrayList<>()).add(longAlgebraic);
                            
                            // Make the move on our board
                            currentBoard = move.execute();
                            moveCount++;
                        }
                    } catch (Exception e) {
                        // Skip problematic moves but continue processing
                        System.err.println("Error processing move " + moveStr + ": " + e.getMessage());
                        continue;
                    }
                }
                
                // Clear references to help GC
                currentBoard = null;
                moveArray = null;
                
            } catch (OutOfMemoryError e) {
                System.err.println("Memory warning: Skipping remaining games");
                break;
            } catch (Exception e) {
                System.err.println("Error processing game: " + e.getMessage());
                continue;
            }
            
            gamesProcessed++;
            if (gamesProcessed % 1000 == 0) {
                long currentTime = System.currentTimeMillis();
                double progress = (gamesProcessed * 100.0) / totalGames;
                double timeInSeconds = (currentTime - startTime) / 1000.0;
                double gamesPerSecond = gamesProcessed / timeInSeconds;
                System.out.printf("Processing games: %.1f%% complete (%,d/%,d) - %.1f games/sec%n", 
                    progress, gamesProcessed, totalGames, gamesPerSecond);
                
                // Force garbage collection every 1000 games
                System.gc();
            }
        }
        
        // Add some basic opening moves in case database is empty
        addBasicOpenings(book);
        
        System.out.printf("Processed %,d games into %,d unique positions%n", 
            gamesProcessed, book.size());
        
        return book;
    }
    
    private void addBasicOpenings(Map<String, List<String>> book) {
        // Initial position
        String startPos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        addBookMove(book, startPos, "e2e4"); // 1.e4
        addBookMove(book, startPos, "d2d4"); // 1.d4
        addBookMove(book, startPos, "g1f3"); // 1.Nf3
        
        // After 1.e4
        String afterE4 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";
        addBookMove(book, afterE4, "e7e5"); // 1...e5
        addBookMove(book, afterE4, "c7c5"); // 1...c5
        addBookMove(book, afterE4, "e7e6"); // 1...e6
        addBookMove(book, afterE4, "c7c6"); // 1...c6
    }
    
    private void addBookMove(Map<String, List<String>> book, String position, String move) {
        book.computeIfAbsent(getPositionKey(position), k -> new ArrayList<>()).add(move);
    }
    
    private String getPositionKey(String fen) {
        String[] parts = fen.split(" ");
        // Join the first 4 parts: piece placement, active color, castling rights, en passant
        return String.join(" ", parts[0], parts[1], parts[2], parts[3]);
    }
} 