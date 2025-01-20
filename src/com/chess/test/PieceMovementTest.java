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
import com.chess.model.moves.capturing.CapturingMove;
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
        
        Collection<Move> knightMoves = whiteKnight.calculateMoves(tiles, board.getOpponentPlayer());
 
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
        
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
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
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
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
        
        Collection<Move> kingMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        Collection<Move> knightMoves = whiteKnight.calculateMoves(tiles, board.getOpponentPlayer());
        
        // King should have moves to escape check
        assertFalse(kingMoves.isEmpty());
        // Knight should have no moves as it can't block or capture the queen
        assertTrue(knightMoves.isEmpty());
    }
    
    @Test
    void testBlockCheck() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); 
        King blackKing = new King(12, Alliance.BLACK); 
        Bishop whiteBishop = new Bishop(58, Alliance.WHITE); 
        Rook blackRook = new Rook(4, Alliance.BLACK); 
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should be able to move to e3 to block the check
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 44)); 
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
        
        Collection<Move> queenMoves = whiteQueen.calculateMoves(tiles, board.getOpponentPlayer());
        
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
        assertFalse(whiteRook.calculateMoves(tiles, board.getOpponentPlayer()).isEmpty());
        assertFalse(whiteBishop.calculateMoves(tiles, board.getOpponentPlayer()).isEmpty());
        assertFalse(whiteQueen.calculateMoves(tiles, board.getOpponentPlayer()).isEmpty());
        assertFalse(whiteKnight.calculateMoves(tiles, board.getOpponentPlayer()).isEmpty());
        assertFalse(whiteKing.calculateMoves(tiles, board.getOpponentPlayer()).isEmpty());
        assertFalse(whitePawn.calculateMoves(tiles, board.getOpponentPlayer()).isEmpty());
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
    
    @Test
    void testQueenBlockingCheck() {
        // Both kings must be present
        King blackKing = new King(4, Alliance.BLACK); 
        King whiteKing = new King(60, Alliance.WHITE); 
        Queen blackQueen = new Queen(63, Alliance.BLACK); 
        Queen whiteQueen = new Queen(13, Alliance.WHITE); 
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(blackKing, whiteKing, blackQueen, whiteQueen));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> queenMoves = blackQueen.calculateMoves(tiles, board.getCurrentPlayer());
        
        // Black queen should be able to move to c5 to block the check
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 61));
    }
    
    @Test
    void testSlidingPiecesNormalMovement() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // Test rook movement in all directions
        Rook whiteRook = new Rook(28, Alliance.WHITE); // e4
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteRook));
        List<Tile> tiles = board.getTiles();
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Rook should be able to move vertically and horizontally
        assertTrue(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 20)); // e5
        assertTrue(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 36)); // e3
        assertTrue(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 29)); // f4
        assertTrue(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 27)); // d4
        
        // Test bishop movement in all directions
        Bishop whiteBishop = new Bishop(28, Alliance.WHITE); // e4
        board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop));
        tiles = board.getTiles();
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should be able to move diagonally
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 21)); // f5
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 35)); // d3
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 19)); // d5
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 37)); // f3
        
        // Test queen movement in all directions
        Queen whiteQueen = new Queen(28, Alliance.WHITE); // e4
        board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteQueen));
        tiles = board.getTiles();
        Collection<Move> queenMoves = whiteQueen.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Queen should be able to move both like rook and bishop
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 20)); // e5 (vertical)
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 29)); // f4 (horizontal)
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 21)); // f5 (diagonal)
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 35)); // d3 (diagonal)
    }
    
    @Test
    void testSlidingPiecesBlockedByOwnPieces() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // Test rook blocked by own pieces
        Rook whiteRook = new Rook(28, Alliance.WHITE); // e4
        Pawn whitePawn1 = new Pawn(20, Alliance.WHITE); // e5
        Pawn whitePawn2 = new Pawn(29, Alliance.WHITE); // f4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, whitePawn1, whitePawn2));
        List<Tile> tiles = board.getTiles();
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Rook should not be able to move through or to squares occupied by own pieces
        assertFalse(rookMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 20 || move.getTargetCoordinate() == 29));
            
        // Test bishop blocked by own pieces
        Bishop whiteBishop = new Bishop(28, Alliance.WHITE); // e4
        Pawn whitePawn3 = new Pawn(21, Alliance.WHITE); // f5
        Pawn whitePawn4 = new Pawn(37, Alliance.WHITE); // f3
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteBishop, whitePawn3, whitePawn4));
        tiles = board.getTiles();
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should not be able to move through or to squares occupied by own pieces
        assertFalse(bishopMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 21 || move.getTargetCoordinate() == 37));
    }
    
    @Test
    void testSlidingPiecesCaptureEnemyPieces() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // Test rook capturing enemy pieces
        Rook whiteRook = new Rook(28, Alliance.WHITE); // e4
        Pawn blackPawn1 = new Pawn(20, Alliance.BLACK); // e5
        Pawn blackPawn2 = new Pawn(29, Alliance.BLACK); // f4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, blackPawn1, blackPawn2));
        List<Tile> tiles = board.getTiles();
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Rook should be able to capture enemy pieces
        assertTrue(rookMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 20 &&  move instanceof CapturingMove));
        assertTrue(rookMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 29 &&  move instanceof CapturingMove));
            
        // Test bishop capturing enemy pieces
        Bishop whiteBishop = new Bishop(28, Alliance.WHITE); // e4
        Pawn blackPawn3 = new Pawn(21, Alliance.BLACK); // f5
        Pawn blackPawn4 = new Pawn(37, Alliance.BLACK); // f3
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteBishop, blackPawn3, blackPawn4));
        tiles = board.getTiles();
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should be able to capture enemy pieces
        assertTrue(bishopMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 21 && move instanceof CapturingMove));
        assertTrue(bishopMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 37 &&  move instanceof CapturingMove));
    }
    
    @Test
    void testSlidingPiecesInCheck() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // Test rook blocking check
        Rook whiteRook = new Rook(21, Alliance.WHITE); // e4
        Queen blackQueen = new Queen(63, Alliance.BLACK); // h1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, blackQueen));
        List<Tile> tiles = board.getTiles();
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Rook should be able to block check by moving to F1
        assertTrue(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 61)); // e2
        
        // Test bishop blocking check
        Bishop whiteBishop = new Bishop(38, Alliance.WHITE); // d3
        Rook blackRook = new Rook(56, Alliance.BLACK); // a1
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteBishop, blackRook));
        tiles = board.getTiles();
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should be able to block check by moving to c2
        assertTrue(bishopMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 59)); // c2
        
        // Test queen blocking check
        Queen whiteQueen = new Queen(43, Alliance.WHITE); // d3
        Bishop blackBishop = new Bishop(18, Alliance.BLACK); // a8
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteQueen, blackBishop));
        tiles = board.getTiles();
        Collection<Move> queenMoves = whiteQueen.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Queen should be able to block check by moving to c2
        assertTrue(queenMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 11)); // c2
    }
    
    @Test
    void testSlidingPiecesPinned() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // Test pinned rook
        Rook whiteRook = new Rook(52, Alliance.WHITE); // e2
        Queen blackQueen = new Queen(44, Alliance.BLACK); // e3
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteRook, blackQueen));
        List<Tile> tiles = board.getTiles();
        Collection<Move> rookMoves = whiteRook.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Rook should only be able to move vertically (along the pin)
        assertTrue(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 44)); // e3 (capture queen)
        assertFalse(rookMoves.stream().anyMatch(move -> move.getTargetCoordinate() == 53)); // f2 (horizontal move)
        
        // Test pinned bishop
        Bishop whiteBishop = new Bishop(52, Alliance.WHITE); // e2
        Rook blackRook = new Rook(36, Alliance.BLACK); // e4
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whiteBishop, blackRook));
        tiles = board.getTiles();
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should have no legal moves when pinned orthogonally
        assertTrue(bishopMoves.isEmpty());
    }
} 