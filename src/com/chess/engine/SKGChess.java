package com.chess.engine;

import com.chess.engine.board.Board;
import com.chess.engine.pieces.test.*;

public class SKGChess {

    public static void main(String[] args) {
        System.out.println("SKG Chess Engine");
        
        Board board = Board.createStandardBoard();
        System.out.println("Initial Board State:");
        System.out.println(board);
        
        System.out.println("\nTesting Rook Moves:");
        RookTest.testRookMovesWithStandardBoard();
        
        System.out.println("\nTesting Knight Moves:");
        KnightTest.testKnightMovesWithStandardBoard();
        
        System.out.println("\nTesting Bishop Moves:");
        BishopTest.testBishopMovesWithStandardBoard();
        
        System.out.println("\nTesting Queen Moves:");
        QueenTest.testQueenMovesWithStandardBoard();
        
        System.out.println("\nTesting King Moves:");
        KingTest.testKingMovesWithStandardBoard();
    }
}