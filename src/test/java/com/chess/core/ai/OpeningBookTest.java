package com.chess.core.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.chess.core.board.Board;
import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.core.player.ai.engine.evaluation.StandardBoardEvaluator;
import com.chess.core.player.ai.engine.strategy.MinimaxStrategy;
import com.chess.core.player.ai.engine.strategy.MoveStrategy;
import com.chess.core.player.ai.engine.strategy.OpeningBookStrategy;
import com.chess.core.Game;
import java.util.Map;

class OpeningBookTest {

    @Test
    void testOpeningBook() {
        IBoard board = Board.createStandardBoard("test");
        Map<String, Game> gameMap = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 4);
        OpeningBookStrategy openingBook = new OpeningBookStrategy(minimaxStrategy);
        
        // Test e4
        Move move = openingBook.getBestMove(board, "test");
        assertNotNull(move, "Expected move for e4");
        assertEquals(52, move.getSourceCoordinate()); // e2
        assertEquals(36, move.getTargetCoordinate()); // e4
        
        // Test d4
        board = Board.createStandardBoard("test");
        Map<String, Game> gameMap1 = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        move = openingBook.getBestMove(board, "test");
        assertNotNull(move, "Expected move for d4");
        
        // Test Nf3
        board = Board.createStandardBoard("test");
        Map<String, Game> gameMap2 = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        move = openingBook.getBestMove(board, "test");
        assertNotNull(move, "Expected move for Nf3");
        
        // Test c4
        board = Board.createStandardBoard("test");
        Map<String, Game> gameMap3 = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        move = openingBook.getBestMove(board, "test");
        assertNotNull(move, "Expected move for c4");
        
        // Test e6
        board = Board.createStandardBoard("test");
        Map<String, Game> gameMap4 = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        move = openingBook.getBestMove(board, "test");
        assertNotNull(move, "Expected move for e6");
    }
    
    @Test
    void testOpeningBookResponse() {
        // Create a board and make e4
        IBoard board = Board.createStandardBoard("test");
        Map<String, Game> gameMap = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        Move e4Move = null;
        
        // Find e4 among legal moves
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 52 && move.getTargetCoordinate() == 36) {
                e4Move = move;
                break;
            }
        }
        assertNotNull(e4Move, "e4 move should be found");
        
        // Make the move to get to the position we want to test
        board = e4Move.execute("test");
        
        // Create the strategy
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Get Black's response
        Move response = bookStrategy.getBestMove(board, "test");
        
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
    void testFallbackToMinimax() {
        // Create a standard board and make a non-book move
        IBoard board = Board.createStandardBoard("test");
        Map<String, Game> gameMap = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        Move nonBookMove = null;
        
        // Find h3 (not in opening book) among legal moves
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 55 && move.getTargetCoordinate() == 47) { // h2h3
                nonBookMove = move;
                break;
            }
        }
        assertNotNull(nonBookMove, "h2h3 move should be found");
        
        // Make the move to get to a non-book position
        board = nonBookMove.execute("test");
        
        // Create the strategy
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Try to get a move - should fall back to minimax
        Move move = bookStrategy.getBestMove(board, "test");
        
        // We should get a valid move from minimax, not null
        assertNotNull(move, "Minimax should find a move");
    }

    @Test
    void testOpeningBookSecondMove() {
        // Create a board and make e4
        IBoard board = Board.createStandardBoard("test");
        Map<String, Game> gameMap = Game.createNewTestGame("test");
Game.addTestGame(board, "test");
        Move e4Move = null;
        
        // Find e4 among legal moves
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 52 && move.getTargetCoordinate() == 36) {
                e4Move = move;
                break;
            }
        }
        assertNotNull(e4Move, "e4 move should be found");
        
        // Make e4
        board = e4Move.execute("test");
        
        // Find and make e5
        Move e5Move = null;
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == 12 && move.getTargetCoordinate() == 28) {
                e5Move = move;
                break;
            }
        }
        assertNotNull(e5Move, "e5 move should be found");
        
        // Make e5
        board = e5Move.execute("test");
        
        // Create the strategy
        MoveStrategy minimaxStrategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        MoveStrategy bookStrategy = new OpeningBookStrategy(minimaxStrategy);
        
        // Get White's second move
        Move response = bookStrategy.getBestMove(board, "test");
        
        // The move should be Nf3
        assertEquals(62, response.getSourceCoordinate()); // g1
        assertEquals(45, response.getTargetCoordinate()); // f3
    }
} 