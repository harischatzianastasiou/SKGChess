package com.chess.test.core.ai;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.chess.core.player.ai.engine.strategy.MinimaxStrategy;
import com.chess.core.player.ai.engine.strategy.MoveStrategy;
import com.chess.core.player.ai.engine.strategy.OpeningBookStrategy;
import  com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import com.chess.core.player.ai.engine.evaluation.StandardBoardEvaluator;

public class OpeningBookTest {

    @Test
    public void testOpeningBookFirstMove() {
        // Create a new board in starting position
        IBoard board = IBoard.createStandardBoard();
        
        // Create the strategy with Minimax as fallback
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Get the best move from the opening position
        Move move = bookStrategy.getBestMove(board);
        
        // The move should be e2e4 (King's Pawn Opening)
        assertEquals(52, move.getSourceCoordinate()); // e2
        assertEquals(36, move.getTargetCoordinate()); // e4
    }
    
    @Test
    public void testOpeningBookResponse() {
        // Create a board and make e4
        IBoard board = IBoard.createStandardBoard();
        Move e4Move = null;
        
        // Find e4 among legal moves
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 52 && move.getTargetCoordinate() == 36) {
                e4Move = move;
                break;
            }
        }
        assertNotNull("e4 move should be found", e4Move);
        
        // Make the move to get to the position we want to test
        board = e4Move.execute();
        
        // Debug output
        System.out.println("\nDebug - Position after e4:");
        String actualFen = board.getFEN();
        String expectedFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
        System.out.println("Actual FEN:   " + actualFen);
        System.out.println("Expected FEN: " + expectedFen);
        System.out.println("FEN strings match: " + actualFen.equals(expectedFen));
        if (!actualFen.equals(expectedFen)) {
            System.out.println("\nCharacter comparison:");
            for (int i = 0; i < Math.min(actualFen.length(), expectedFen.length()); i++) {
                if (actualFen.charAt(i) != expectedFen.charAt(i)) {
                    System.out.printf("Mismatch at position %d: actual='%c' expected='%c'\n", 
                        i, actualFen.charAt(i), expectedFen.charAt(i));
                }
            }
            if (actualFen.length() != expectedFen.length()) {
                System.out.println("Length mismatch: actual=" + actualFen.length() + 
                    " expected=" + expectedFen.length());
            }
        }
        
        // Create the strategy
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Get Black's response
        Move response = bookStrategy.getBestMove(board);
        
        // Debug output
        System.out.println("\nDebug - Response move:");
        System.out.println("Source coordinate: " + response.getSourceCoordinate());
        System.out.println("Target coordinate: " + response.getTargetCoordinate());
        
        // The move should be one of the main responses to 1.e4
        assertTrue(
            // e7e5 (Open Game)
            (response.getSourceCoordinate() == 12 && response.getTargetCoordinate() == 28) ||
            // c7c5 (Sicilian)
            (response.getSourceCoordinate() == 10 && response.getTargetCoordinate() == 26) ||
            // e7e6 (French)
            (response.getSourceCoordinate() == 12 && response.getTargetCoordinate() == 20) ||
            // c7c6 (Caro-Kann)
            (response.getSourceCoordinate() == 10 && response.getTargetCoordinate() == 18),
            "Response should be a valid book move");
    }
    
    @Test
    public void testFallbackToMinimax() {
        // Create a standard board and make a non-book move
        IBoard board = IBoard.createStandardBoard();
        Move nonBookMove = null;
        
        // Find h3 (not in opening book) among legal moves
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 55 && move.getTargetCoordinate() == 47) { // h2h3
                nonBookMove = move;
                break;
            }
        }
        assertNotNull("h2h3 move should be found", nonBookMove);
        
        // Make the move to get to a non-book position
        board = nonBookMove.execute();
        
        // Create the strategy
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Try to get a move - should fall back to minimax
        Move move = bookStrategy.getBestMove(board);
        
        // We should get a valid move from minimax, not null
        assertNotNull("Minimax should find a move", move);
    }

    @Test
    public void testOpeningBookSecondMove() {
        // Create a board and make e4
        IBoard board = IBoard.createStandardBoard();
        Move e4Move = null;
        
        // Find e4 among legal moves
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 52 && move.getTargetCoordinate() == 36) {
                e4Move = move;
                break;
            }
        }
        assertNotNull("e4 move should be found", e4Move);
        
        // Make e4
        board = e4Move.execute();
        
        // Find and make e5
        Move e5Move = null;
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 12 && move.getTargetCoordinate() == 28) {
                e5Move = move;
                break;
            }
        }
        assertNotNull("e5 move should be found", e5Move);
        
        // Make e5
        board = e5Move.execute();
        
        // Debug output
        System.out.println("\nDebug - Position after e4 e5:");
        System.out.println("FEN: " + board.getFEN());
        
        // Create the strategy
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Get White's second move
        Move response = bookStrategy.getBestMove(board);
        
        // Debug output
        System.out.println("\nDebug - White's second move:");
        System.out.println("Source coordinate: " + response.getSourceCoordinate());
        System.out.println("Target coordinate: " + response.getTargetCoordinate());
        
        // The move should be Nf3
        assertEquals(62, response.getSourceCoordinate()); // g1
        assertEquals(45, response.getTargetCoordinate()); // f3
    }
} 