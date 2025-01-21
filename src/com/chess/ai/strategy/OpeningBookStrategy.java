package com.chess.ai.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.chess.ai.utils.MoveConverter;
import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;

public class OpeningBookStrategy implements MoveStrategy {
    private final Map<String, List<String>> openingBook;
    private final MoveStrategy fallbackStrategy;
    private final Random random;

    public OpeningBookStrategy(MoveStrategy fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
        this.openingBook = initializeOpeningBook();
        this.random = new Random();
    }

    @Override
    public Move getBestMove(IBoard board) {
        String position = board.getFEN();
        // Extract the important parts of the FEN (pieces, side to move, castling, en passant)
        String positionKey = getPositionKey(position);
        List<String> bookMoves = null;
        
        // Find matching position in book, ignoring move counters
        for (Map.Entry<String, List<String>> entry : openingBook.entrySet()) {
            if (getPositionKey(entry.getKey()).equals(positionKey)) {
                bookMoves = entry.getValue();
                break;
            }
        }
        
        System.out.println("\n=== Opening Book Position ===");
        System.out.println("Looking for position key: " + positionKey);
        System.out.println("Found book moves: " + bookMoves);
        
        // Debug: Print all book positions that start with the same piece placement
        String piecePlacement = position.split(" ")[0];  // Get just the piece placement part
        System.out.println("\nAll similar positions in book:");
        openingBook.forEach((fen, moves) -> {
            String bookPiecePlacement = fen.split(" ")[0];
            if (bookPiecePlacement.equals(piecePlacement)) {
                System.out.println("Book FEN: " + fen);
                System.out.println("Book moves: " + moves);
                System.out.println("Pieces match but full FEN " + (fen.equals(position) ? "matches" : "differs") + "\n");
            }
        });
        
        if (bookMoves != null && !bookMoves.isEmpty()) {
            // Randomly select one of the book moves
            String bookMove = bookMoves.get(random.nextInt(bookMoves.size()));
            Move move = MoveConverter.fromLongAlgebraic(bookMove, board);
            if (move != null) {
                System.out.println("✓ Using book move: " + bookMove);
                return move;
            }
        }
        
        System.out.println("❌ No book move found, using fallback strategy");
        return fallbackStrategy.getBestMove(board);
    }

    @Override
    public String getName() {
        return "Opening Book + " + fallbackStrategy.getName();
    }

    private Map<String, List<String>> initializeOpeningBook() {
        Map<String, List<String>> book = new HashMap<>();
        
        // 1. e4 lines
        addE4Lines(book);
        
        // 1. d4 lines
        addD4Lines(book);
        
        // 1. Nf3 lines (Reti Opening)
        addNf3Lines(book);
        
        return book;
    }
    
    private void addBookMove(Map<String, List<String>> book, String position, String move) {
        book.computeIfAbsent(position, k -> new ArrayList<>()).add(move);
    }
    
    private void addE4Lines(Map<String, List<String>> book) {
        // Initial position
        addBookMove(book, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4");
        
        // 1. e4
        String afterE4 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";
        
        // 1...e5 (Open Game)
        addBookMove(book, afterE4, "e7e5");
        addBookMove(book, "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", "g1f3");  // 2. Nf3
        addBookMove(book, "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2", "b8c6");  // 2...Nc6
        
        // 1...c5 (Sicilian Defense)
        addBookMove(book, afterE4, "c7c5");
        addBookMove(book, "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", "g1f3");  // 2. Nf3
        
        // 1...e6 (French Defense)
        addBookMove(book, afterE4, "e7e6");
        addBookMove(book, "rnbqkbnr/pppp1ppp/4p3/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", "d2d4");  // 2. d4
        
        // 1...c6 (Caro-Kann)
        addBookMove(book, afterE4, "c7c6");
        addBookMove(book, "rnbqkbnr/pp1ppppp/2p5/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", "d2d4");  // 2. d4
    }
    
    private void addD4Lines(Map<String, List<String>> book) {
        // 1. d4
        addBookMove(book, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "d2d4");
        
        String afterD4 = "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1";
        
        // 1...d5 (Closed Game)
        addBookMove(book, afterD4, "d7d5");
        addBookMove(book, "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 2", "c2c4");  // 2. c4
        
        // 1...Nf6 (Indian Defense)
        addBookMove(book, afterD4, "g8f6");
        addBookMove(book, "rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 1 2", "c2c4");  // 2. c4
    }
    
    private void addNf3Lines(Map<String, List<String>> book) {
        // 1. Nf3 (Reti Opening)
        addBookMove(book, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "g1f3");
        
        String afterNf3 = "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1";
        
        // 1...d5
        addBookMove(book, afterNf3, "d7d5");
        addBookMove(book, "rnbqkbnr/ppp1pppp/8/3p4/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 0 2", "c2c4");  // 2. c4
    }
    
    // Helper method to get position key without move counters
    private String getPositionKey(String fen) {
        String[] parts = fen.split(" ");
        // Join the first 4 parts: piece placement, active color, castling rights, en passant
        return String.join(" ", parts[0], parts[1], parts[2], parts[3]);
    }
} 