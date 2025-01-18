package com.chess.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Arrays;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.*;
import com.chess.model.tiles.Tile;
import java.util.List;
import com.chess.model.moves.*;
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.capturing.PawnPromotionCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
//import com.chess.model.moves.PawnPromotion;
//import com.chess.model.moves.EnPassantMove;
//import com.chess.model.moves.PawnJump;
import com.chess.model.moves.noncapturing.PawnPromotionMove;

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

        
    @Test
    void testPawnPromotion() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // White pawn about to promote
        Pawn whitePawn = new Pawn(8, Alliance.WHITE); // a7
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Pawn should have promotion moves available
        assertTrue(pawnMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 0 && // a8
            move instanceof PawnPromotionMove));
            
        // Test black pawn promotion
        Pawn blackPawn = new Pawn(55, Alliance.BLACK); // h2
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackPawn));
        tiles = board.getTiles();
        
        pawnMoves = blackPawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Black pawn should have promotion moves
        assertTrue(pawnMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 63 && // h1
            move instanceof PawnPromotionMove));
    }
    
    @Test
    void testEnPassantCapture() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // White pawn in position for en passant
        Pawn whitePawn = new Pawn(35, Alliance.WHITE); // d5
        // Black pawn makes two-square advance
        Pawn blackPawn = new Pawn(51, Alliance.BLACK); // e7
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn, blackPawn));
        List<Tile> tiles = board.getTiles();
        
        // Move black pawn two squares forward
        Collection<Move> blackPawnMoves = blackPawn.calculateMoves(tiles, board.getCurrentPlayer());
        Move twoSquareAdvance = blackPawnMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 35)
            .findFirst()
            .orElse(null);
        assertNotNull(twoSquareAdvance);
        
        board = twoSquareAdvance.execute();
        tiles = board.getTiles();
        
        // White pawn should have en passant capture available
        Collection<Move> whitePawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        assertTrue(whitePawnMoves.stream().anyMatch(move -> 
            move instanceof PawnEnPassantAttack &&
            move.getTargetCoordinate() == 42)); // e6
            
        // Test black en passant capture
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing,
            new Pawn(28, Alliance.BLACK), // e4
            new Pawn(8, Alliance.WHITE))); // a7
        tiles = board.getTiles();
        
        // Move white pawn two squares
        Move whiteTwoSquare = new PawnJumpMove(tiles, 8, 24, whitePawn); // a7 to a5
        board = whiteTwoSquare.execute();
        tiles = board.getTiles();
        
        // Black pawn should have en passant capture
        Collection<Move> blackMoves = board.getTiles().get(28).getPiece().calculateMoves(tiles, board.getOpponentPlayer());
        assertTrue(blackMoves.stream().anyMatch(move -> 
            move instanceof PawnEnPassantAttack &&
            move.getTargetCoordinate() == 23)); // a6
    }
    
    @Test
    void testPawnPromotionWithCapture() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // White pawn about to promote with capture
        Pawn whitePawn = new Pawn(8, Alliance.WHITE); // a7
        Rook blackRook = new Rook(1, Alliance.BLACK); // b8
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Pawn should have promotion capture move available
        assertTrue(pawnMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 1 && // b8
            move instanceof PawnPromotionCapturingMove));
    }
    
    @Test
    void testPawnBlockedPromotion() {
        // Both kings must be present
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        
        // White pawn blocked from promotion
        Pawn whitePawn = new Pawn(8, Alliance.WHITE); // a7
        Rook blackRook = new Rook(0, Alliance.BLACK); // a8
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Pawn should not have any promotion moves when blocked
        assertFalse(pawnMoves.stream().anyMatch(move -> 
            move instanceof PawnPromotionMove));
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
} 