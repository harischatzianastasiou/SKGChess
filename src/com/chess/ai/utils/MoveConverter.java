package com.chess.ai.utils;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;

public class MoveConverter {
    
    // Map chess files (a-h) to board columns (0-7)
    private static final int[] FILE_MAP = {0, 1, 2, 3, 4, 5, 6, 7};  // a=0, b=1, etc.
    
    // Map chess ranks (1-8) to board rows (0-7), counting from top to bottom
    private static final int[] RANK_MAP = {0, 1, 2, 3, 4, 5, 6, 7};  // 8=0, 7=1, etc.
    
    public static Move fromLongAlgebraic(String notation, IBoard board) {
        if (notation == null || notation.length() != 4) {
            System.out.println("Invalid notation length: " + notation);
            return null;
        }
        
        System.out.println("\n=== Converting Move: " + notation + " ===");
        
        // Get files (a-h)
        char fromFileChar = notation.charAt(0);
        char toFileChar = notation.charAt(2);
        
        // Get ranks (1-8)
        int fromRankNum = Character.getNumericValue(notation.charAt(1));
        int toRankNum = Character.getNumericValue(notation.charAt(3));
        
        // Convert to board coordinates
        int fromFile = fromFileChar - 'a';
        int toFile = toFileChar - 'a';
        int fromRank = RANK_MAP[8 - fromRankNum];  // Convert chess rank (8-1) to board rank (0-7)
        int toRank = RANK_MAP[8 - toRankNum];
        
        System.out.println(String.format("From: %c%d -> rank=%d, file=%d", fromFileChar, fromRankNum, fromRank, fromFile));
        System.out.println(String.format("To: %c%d -> rank=%d, file=%d", toFileChar, toRankNum, toRank, toFile));
        
        // Calculate board coordinates
        int sourceCoordinate = (fromRank * 8) + fromFile;
        int targetCoordinate = (toRank * 8) + toFile;
        
        System.out.println("Board coordinates: " + sourceCoordinate + " -> " + targetCoordinate);
        
        // Validate coordinates
        if (!isValidCoordinate(sourceCoordinate) || !isValidCoordinate(targetCoordinate)) {
            System.out.println("❌ Invalid coordinates!");
            return null;
        }
        
        // Get the piece at the source square
        Tile sourceTile = board.getTile(sourceCoordinate);
        if (!sourceTile.isTileOccupied()) {
            System.out.println("❌ No piece at source square!");
            return null;
        }
        
        Piece piece = sourceTile.getPiece();
        System.out.println("Piece at source: " + piece.getPieceSymbol());
        
        // Find the matching legal move
        for (Move move : piece.calculateMoves(board.getTiles(), board.getOpponentPlayer())) {
            if (move.getSourceCoordinate() == sourceCoordinate && 
                move.getTargetCoordinate() == targetCoordinate) {
                System.out.println("✓ Found legal move!");
                return move;
            }
        }
        
        System.out.println("❌ No legal move found!");
        return null;
    }
    
    private static boolean isValidCoordinate(int coordinate) {
        return coordinate >= 0 && coordinate < 64;
    }
}
