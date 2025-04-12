package com.chess.core.utils;

import com.chess.core.board.IBoard;

/**
 * Test class to demonstrate FEN conversion functionality
 */
public class FenTest {

    /**
     * Main method to test FEN conversion
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // The standard starting position FEN
        String startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        
        // Convert FEN to Board
        System.out.println("Converting FEN to Board...");
        IBoard board = FenUtils.fenToBoard(startingFen, null, false);
        
        // Print the board
        System.out.println("Board created from FEN:");
        System.out.println(board);
        
        // Convert Board back to FEN
        System.out.println("\nConverting Board back to FEN...");
        String fen = FenUtils.boardToFen(board);
        
        // Print the FEN
        System.out.println("FEN created from Board:");
        System.out.println(fen);
        
        // Compare the original FEN with the new FEN
        System.out.println("\nFENs match: " + startingFen.equals(fen));
        
        // Test with a different position
        String customFen = "r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 0 1";
        
        System.out.println("\n\nTesting with a custom position...");
        IBoard customBoard = FenUtils.fenToBoard(customFen, null, false);
        
        System.out.println("Custom Board:");
        System.out.println(customBoard);
        
        String customFenResult = FenUtils.boardToFen(customBoard);
        System.out.println("\nFEN from custom Board:");
        System.out.println(customFenResult);
        
        System.out.println("\nFENs match: " + customFen.equals(customFenResult));
        
        // Test algebraic notation conversion
        System.out.println("\n\nTesting algebraic notation conversion...");
        
        // Test coordinate to algebraic
        int[] testCoordinates = {0, 7, 56, 63, 27, 36};
        for (int coord : testCoordinates) {
            String algebraic = FenUtils.coordinateToAlgebraic(coord);
            System.out.println("Coordinate " + coord + " -> Algebraic " + algebraic);
            
            // Test algebraic to coordinate
            int backToCoord = FenUtils.algebraicToCoordinate(algebraic);
            System.out.println("Algebraic " + algebraic + " -> Coordinate " + backToCoord);
            System.out.println("Conversion successful: " + (coord == backToCoord));
        }
    }
} 