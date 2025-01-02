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
import java.util.List;

class PawnMovementTest {
    
    @Test
    void testPawnRegularMove() {
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(44, Alliance.WHITE); // e4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Should only be able to move one square forward
        assertTrue(moves.stream().anyMatch(move -> move.getTargetCoordinate() == 36)); // e4
        assertFalse(moves.stream().anyMatch(move -> move.getTargetCoordinate() == 28)); // e5
    }
    
    @Test
    void testPawnCapture() {
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(44, Alliance.WHITE); // e4
        Pawn blackPawnLeft = new Pawn(35, Alliance.BLACK); // d5
        Pawn blackPawnRight = new Pawn(37, Alliance.BLACK); // f5
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn, blackPawnLeft, blackPawnRight));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Should be able to capture diagonally
        assertTrue(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 35 && move.getCapturedPiece() != null)); // d4
        assertTrue(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 37 && move.getCapturedPiece() != null)); // f4
    }
    
    @Test
    void testPawnBlockedMovement() {
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(52, Alliance.WHITE); // e2
        Pawn blockingPawn = new Pawn(44, Alliance.BLACK); // e4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn, blockingPawn));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Should not be able to move forward when blocked
        assertFalse(moves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 44 || move.getTargetCoordinate() == 36));
    }
} 