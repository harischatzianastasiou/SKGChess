package com.chess.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.*;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;

class PieceMovementTest {
    
    @Test
    void testPinnedPieceHorizontal() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Knight whiteKnight = new Knight(61, Alliance.WHITE); // f1
        Rook blackRook = new Rook(63, Alliance.BLACK); // h1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteKnight, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> knightMoves = whiteKnight.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Knight should not be able to move as it's pinned horizontally
        assertTrue(knightMoves.isEmpty());
    }
    
    @Test
    void testPinnedPieceVertical() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(23, Alliance.BLACK); // e8
        Bishop whiteBishop = new Bishop(52, Alliance.WHITE); // e2
        Queen blackQueen = new Queen(4, Alliance.BLACK); // e8
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop, blackQueen));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Bishop should not be able to move as it's pinned vertically
        assertTrue(bishopMoves.isEmpty());
    }
    
    @Test
    void testPinnedPieceDiagonal() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(53, Alliance.WHITE); // f2
        Bishop blackBishop = new Bishop(39, Alliance.BLACK); // h4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn, blackBishop));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Pawn should not be able to move as it's pinned diagonally
        assertTrue(pawnMoves.isEmpty());
    }
    
    @Test
    void testMoveWhenInCheck() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Knight whiteKnight = new Knight(57, Alliance.WHITE); // b1
        Queen blackQueen = new Queen(28, Alliance.BLACK); // e4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteKnight, blackQueen));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> kingMoves = whiteKing.calculateMoves(tiles, board.getCurrentPlayer());
        Collection<Move> knightMoves = whiteKnight.calculateMoves(tiles, board.getCurrentPlayer());
        
        // King should have moves to escape check
        assertFalse(kingMoves.isEmpty());
        // Knight should have no moves as it can't block or capture the queen
        assertTrue(knightMoves.isEmpty());
    }
    
    @Test
    void testBlockCheck() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(12, Alliance.BLACK); // e8
        Bishop whiteBishop = new Bishop(58, Alliance.WHITE); // c1
        Rook blackRook = new Rook(4, Alliance.BLACK); // e8
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Bishop should be able to move to e3 to block the check
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 36)); // e3
    }
    
    @Test
    void testCaptureCheckingPiece() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Queen whiteQueen = new Queen(59, Alliance.WHITE); // d1
        Knight blackKnight = new Knight(45, Alliance.BLACK); // f3
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteQueen, blackKnight));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> queenMoves = whiteQueen.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Queen should be able to capture the knight
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 45)); // f3
    }
    
    @Test
    void testNormalPieceMovements() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Rook whiteRook = new Rook(56, Alliance.WHITE); // a1
        Bishop whiteBishop = new Bishop(58, Alliance.WHITE); // c1
        Queen whiteQueen = new Queen(59, Alliance.WHITE); // d1
        Knight whiteKnight = new Knight(57, Alliance.WHITE); // b1
        Pawn whitePawn = new Pawn(52, Alliance.WHITE); // e2
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, whiteBishop, whiteQueen, whiteKnight, whitePawn));
        List<Tile> tiles = board.getTiles();
        
        // Test movements for each piece
        assertFalse(whiteRook.calculateMoves(tiles, board.getCurrentPlayer()).isEmpty());
        assertFalse(whiteBishop.calculateMoves(tiles, board.getCurrentPlayer()).isEmpty());
        assertFalse(whiteQueen.calculateMoves(tiles, board.getCurrentPlayer()).isEmpty());
        assertFalse(whiteKnight.calculateMoves(tiles, board.getCurrentPlayer()).isEmpty());
        assertFalse(whiteKing.calculateMoves(tiles, board.getCurrentPlayer()).isEmpty());
        assertFalse(whitePawn.calculateMoves(tiles, board.getCurrentPlayer()).isEmpty());
    }
    
    @Test
    void testPawnSpecialMoves() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(52, Alliance.WHITE); // e2
        Pawn blackPawn = new Pawn(35, Alliance.BLACK); // d5
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn, blackPawn));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getCurrentPlayer());
        Collection<Move> blackPawnMoves = blackPawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Test initial two-square advance
        assertTrue(pawnMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 36)); // e4
        
        // Test diagonal capture
        assertTrue(blackPawnMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 44)); // e6
    }
    
    @Test
    void testKnightJumping() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Knight whiteKnight = new Knight(57, Alliance.WHITE); // b1
        Pawn whitePawnA = new Pawn(48, Alliance.WHITE); // a2
        Pawn whitePawnC = new Pawn(50, Alliance.WHITE); // c2
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteKnight, whitePawnA, whitePawnC));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> knightMoves = whiteKnight.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Knight should be able to move to a3 and c3 even with pawns in the way
        assertTrue(knightMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 42)); // a3
        assertTrue(knightMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 42)); // c3
    }
} 