package com.chess.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import  static org.junit.jupiter.api.Assertions.assertNotNull;
import  static org.junit.jupiter.api.Assertions.assertTrue;
import  org.junit.jupiter.api.Test;

import  com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.core.moves.capturing.PawnEnPassantAttack;
import  com.chess.core.moves.capturing.PawnPromotionCapturingMove;
import  com.chess.core.moves.noncapturing.PawnJumpMove;
import  com.chess.core.moves.noncapturing.PawnPromotionMove;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Pawn;
import com.chess.core.pieces.Rook;
import com.chess.core.tiles.Tile;
import com.chess.core.GameManager;
import java.util.Map;
class PawnMovementTest {
    
    @Test
    void testPawnRegularMove() {
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(44, Alliance.WHITE); // e4
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        
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
            whiteKing, blackKing, whitePawn, blackPawnLeft, blackPawnRight), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        
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
            whiteKing, blackKing, whitePawn, blockingPawn), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> moves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        
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
            whiteKing, blackKing, whitePawn), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        
        // Pawn should have promotion moves available
        assertTrue(pawnMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 0 && // a8
            move instanceof PawnPromotionMove));
            
        // Test black pawn promotion
        Pawn blackPawn = new Pawn(55, Alliance.BLACK); // h2
        
        board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackPawn),"test");
        Map<String, GameManager> gameMap1 = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        tiles = board.getTiles();
        
        pawnMoves = blackPawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        
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
        Pawn whitePawn = new Pawn(27, Alliance.WHITE); // d5
        // Black pawn makes two-square advance
        Pawn blackPawn = new Pawn(12, Alliance.BLACK); // e7
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, whitePawn, blackPawn), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        // Move black pawn two squares forward
        Collection<Move> blackPawnMoves = blackPawn.calculateMoves(tiles, board.getCurrentPlayer(), "test");
        Move twoSquareAdvance = new PawnJumpMove(tiles, 12, 28, blackPawn);
        assertNotNull(twoSquareAdvance);
        board = twoSquareAdvance.execute("test");
        gameMap.get("test").updateGame(twoSquareAdvance);
        tiles = board.getTiles();
        
        // White pawn should have en passant capture available
        Collection<Move> whitePawnMoves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        assertTrue(whitePawnMoves.stream().anyMatch(move -> 
            move instanceof PawnEnPassantAttack &&
            move.getTargetCoordinate() == 20)); 
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
            whiteKing, blackKing, whitePawn, blackRook), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");

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
            whiteKing, blackKing, whitePawn, blackRook), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getopponentPlayer(), "test");
        
        // Pawn should not have any promotion moves when blocked
        assertFalse(pawnMoves.stream().anyMatch(move -> 
            move instanceof PawnPromotionMove));
    }
} 