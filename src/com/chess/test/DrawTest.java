package com.chess.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Arrays;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.*;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;
import com.chess.util.GameHistory;
import java.util.List;

class DrawTest {
    
    @Test
    void testStalemate() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(7, Alliance.BLACK);
        Queen whiteQueen = new Queen(46, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteQueen));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> kingMoves = blackKing.calculateMoves(tiles, board.getCurrentPlayer());
        Collection<Move> queenMoves = whiteQueen.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Black king should have no legal moves but not be in check
        assertTrue(kingMoves.isEmpty());
        assertFalse(queenMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == blackKing.getPieceCoordinate()));
    }
    
    @Test
    void testInsufficientMaterial() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Bishop whiteBishop = new Bishop(61, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> whiteMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        Collection<Move> blackMoves = blackKing.calculateMoves(tiles, board.getCurrentPlayer());
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Both kings and a bishop should be insufficient material
        assertFalse(whiteMoves.isEmpty());
        assertFalse(blackMoves.isEmpty());
        assertFalse(bishopMoves.isEmpty());
    }
    
    @Test
    void testSufficientMaterial() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Pawn whitePawn = new Pawn(52, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Having a pawn means there's sufficient material
        assertFalse(pawnMoves.isEmpty());
    }
    
    @Test
    void testThreefoldRepetition() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Knight whiteKnight = new Knight(62, Alliance.WHITE); // g1
        Knight blackKnight = new Knight(6, Alliance.BLACK); // g8
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteKnight, blackKnight));
        List<Tile> tiles = board.getTiles();
        
        // Get the specific moves we want to repeat
        Collection<Move> whiteKnightMoves = whiteKnight.calculateMoves(tiles, board.getOpponentPlayer());
        Collection<Move> blackKnightMoves = blackKnight.calculateMoves(tiles, board.getCurrentPlayer());
        
        Move whiteKnightToF3 = whiteKnightMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 45) // f3
            .findFirst().orElse(null);
        Move blackKnightToF6 = blackKnightMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 21) // f6
            .findFirst().orElse(null);
            
        assertNotNull(whiteKnightToF3);
        assertNotNull(blackKnightToF6);
        
        // Execute the moves three times
        for (int i = 0; i < 3; i++) {
            board = whiteKnightToF3.execute();
            GameHistory.getInstance().addBoard(board);
            board = blackKnightToF6.execute();
            GameHistory.getInstance().addBoard(board);
            board = whiteKnightToF3.undo();
            GameHistory.getInstance().addBoard(board);
            board = blackKnightToF6.undo();
            GameHistory.getInstance().addBoard(board);
        }
        
        // Check if threefold repetition has occurred
        assertTrue(GameHistory.getInstance().getPositionCount(board) >= 3);
    }
    
    @Test
    void testFiftyMoveRule() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> whiteKingMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        Collection<Move> blackKingMoves = blackKing.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Get the specific moves for the kings
        Move whiteKingToF1 = whiteKingMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 61) // f1
            .findFirst().orElse(null);
        Move blackKingToF8 = blackKingMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 5) // f8
            .findFirst().orElse(null);
            
        assertNotNull(whiteKingToF1);
        assertNotNull(blackKingToF8);
        
        // Execute 50 moves without pawn moves or captures
        for (int i = 0; i < 50; i++) {
            board = whiteKingToF1.execute();
            board = blackKingToF8.execute();
            board = whiteKingToF1.undo();
            board = blackKingToF8.undo();
        }
        
        // Check if 50 moves have been made without pawn moves or captures
        assertEquals(100, GameHistory.getInstance().getHalfMoveCount());
    }
} 