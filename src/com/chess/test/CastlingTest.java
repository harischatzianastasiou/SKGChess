package com.chess.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Arrays;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Rook;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;
import java.util.List;

class CastlingTest {
    
    @Test
    void testKingsideCastling() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(63, Alliance.WHITE); // h1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        
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
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        
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
            whiteKing, blackKing, whiteRook, blockingRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        
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
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> kingMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Move king
        Move kingMove = kingMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 61)
            .findFirst().orElse(null);
        assertNotNull(kingMove);
        board = kingMove.execute();
        board = kingMove.undo();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        
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
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook));
        List<Tile> tiles = board.getTiles();
        
        // Move rook
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer());
        Move rookMove = rookMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 62)
            .findFirst().orElse(null);
        assertNotNull(rookMove);
        board = rookMove.execute();
        board = rookMove.undo();
        
        Collection<Move> moves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Should not be able to castle after rook has moved
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 62)); // g1
    }
} 