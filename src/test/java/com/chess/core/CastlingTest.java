package com.chess.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import  static org.junit.jupiter.api.Assertions.assertNotNull;
import  static org.junit.jupiter.api.Assertions.assertTrue;
import  org.junit.jupiter.api.Test;

import  com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.Bishop;
import  com.chess.core.pieces.King;
import com.chess.core.pieces.Rook;
import com.chess.core.tiles.Tile;

class CastlingTest {
    
    @Test
    void testKingsideCastling() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(63, Alliance.WHITE); // h1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook), "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should be able to castle kingside
        assertTrue(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
    }
    
    @Test
    void testQueensideCastling() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(56, Alliance.WHITE); // a1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook), "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should be able to castle queenside
        assertTrue(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 58)); // c1
    }
    
    @Test
    void testCastlingBlockedByPieces() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(63, Alliance.WHITE); // h1
        Rook blockingRook = new Rook(61, Alliance.WHITE); // f1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, blockingRook), "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should not be able to castle when pieces are in the way
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
    }
    
    @Test
    void testCastlingPreventedAfterKingMove() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(63, Alliance.WHITE); // h1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook), "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> kingMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Move king
        Move kingMove = kingMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 61)
            .findFirst().orElse(null);
        assertNotNull(kingMove);
        
        board = kingMove.execute("test");
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should not be able to castle after king has moved
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
    }
    
    @Test
    void testCastlingPreventedAfterRookMove() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(63, Alliance.WHITE); // h1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook), "test");
        List<Tile> tiles = board.getTiles();
        
        // Move rook
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        Move rookMove = rookMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 62)
            .findFirst().orElse(null);
        assertNotNull(rookMove);
        board = rookMove.execute("test");
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should not be able to castle after rook has moved
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
    }

    @Test
    void testCastlingPreventedBySlidingPieceControl() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(63, Alliance.WHITE); // h1
        
        // Black rook controls f1 square
        Rook blackRook = new Rook(61, Alliance.BLACK); // f1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, blackRook), "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should not be able to castle when a square is controlled by enemy piece
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
            
        // Test with bishop controlling castling square
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook,
            new Bishop(43, Alliance.BLACK)), "test");
        tiles = board.getTiles();
        
        moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Should not be able to castle when a square is controlled by enemy bishop
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
    }
} 