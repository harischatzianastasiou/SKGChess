package com.chess.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.chess.core.board.IBoard;
import com.chess.core.moves.noncapturing.PawnPromotionMove;
import com.chess.core.moves.capturing.PawnPromotionCapturingMove;
import com.chess.core.pieces.Pawn;
import com.chess.core.pieces.King;
import com.chess.core.pieces.Queen;
import com.chess.core.pieces.Rook;
import com.chess.core.pieces.Bishop;
import com.chess.core.pieces.Knight;
import com.chess.core.tiles.Tile;

import java.util.Arrays;
import java.util.List;

/**
 * Test class to verify that pawn promotion with piece selection works correctly
 * This tests the new functionality where players can choose which piece to promote to
 */
class PromotionPieceSelectionTest {
    
    @Test
    void testPawnPromotionMoveWithPieceSelection() {
        // Create a simple board with a white pawn ready to promote
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(8, Alliance.WHITE); // a7 - ready to promote
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn), "test");
        List<Tile> tiles = board.getTiles();
        
        // Test promotion to Queen
        PawnPromotionMove queenPromotion = new PawnPromotionMove(
            tiles, 8, 0, whitePawn, "QUEEN"
        );
        IBoard queenBoard = queenPromotion.execute();
        
        // Verify the promoted piece is a Queen
        Tile promotedTile = queenBoard.getTile(0);
        assertTrue(promotedTile.isTileOccupied());
        assertEquals(Queen.class, promotedTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, promotedTile.getPiece().getPieceAlliance());
        
        // Test promotion to Rook
        PawnPromotionMove rookPromotion = new PawnPromotionMove(
            tiles, 8, 0, whitePawn, "ROOK"
        );
        IBoard rookBoard = rookPromotion.execute();
        
        // Verify the promoted piece is a Rook
        Tile rookTile = rookBoard.getTile(0);
        assertTrue(rookTile.isTileOccupied());
        assertEquals(Rook.class, rookTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, rookTile.getPiece().getPieceAlliance());
        
        // Test promotion to Bishop
        PawnPromotionMove bishopPromotion = new PawnPromotionMove(
            tiles, 8, 0, whitePawn, "BISHOP"
        );
        IBoard bishopBoard = bishopPromotion.execute();
        
        // Verify the promoted piece is a Bishop
        Tile bishopTile = bishopBoard.getTile(0);
        assertTrue(bishopTile.isTileOccupied());
        assertEquals(Bishop.class, bishopTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, bishopTile.getPiece().getPieceAlliance());
        
        // Test promotion to Knight
        PawnPromotionMove knightPromotion = new PawnPromotionMove(
            tiles, 8, 0, whitePawn, "KNIGHT"
        );
        IBoard knightBoard = knightPromotion.execute();
        
        // Verify the promoted piece is a Knight
        Tile knightTile = knightBoard.getTile(0);
        assertTrue(knightTile.isTileOccupied());
        assertEquals(Knight.class, knightTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, knightTile.getPiece().getPieceAlliance());
    }
    
    @Test
    void testPawnPromotionCapturingMoveWithPieceSelection() {
        // Create a board with a white pawn ready to promote by capturing
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Pawn whitePawn = new Pawn(8, Alliance.WHITE); // a7
        Rook blackRook = new Rook(1, Alliance.BLACK); // b8 - will be captured
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn, blackRook), "test");
        List<Tile> tiles = board.getTiles();
        
        // Test promotion with capture to Queen
        PawnPromotionCapturingMove queenPromotionCapture = new PawnPromotionCapturingMove(
            tiles, 8, 1, whitePawn, blackRook, "QUEEN"
        );
        IBoard queenBoard = queenPromotionCapture.execute();
        
        // Verify the promoted piece is a Queen and the captured piece is gone
        Tile promotedTile = queenBoard.getTile(1);
        assertTrue(promotedTile.isTileOccupied());
        assertEquals(Queen.class, promotedTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, promotedTile.getPiece().getPieceAlliance());
        
        // Test promotion with capture to Knight
        PawnPromotionCapturingMove knightPromotionCapture = new PawnPromotionCapturingMove(
            tiles, 8, 1, whitePawn, blackRook, "KNIGHT"
        );
        IBoard knightBoard = knightPromotionCapture.execute();
        
        // Verify the promoted piece is a Knight
        Tile knightTile = knightBoard.getTile(1);
        assertTrue(knightTile.isTileOccupied());
        assertEquals(Knight.class, knightTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, knightTile.getPiece().getPieceAlliance());
    }
    
    @Test
    void testDefaultPromotionBehavior() {
        // Test that the default constructor still works (promotes to Queen)
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Pawn whitePawn = new Pawn(8, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn), "test");
        List<Tile> tiles = board.getTiles();
        
        // Test default promotion (should be Queen)
        PawnPromotionMove defaultPromotion = new PawnPromotionMove(
            tiles, 8, 0, whitePawn
        );
        IBoard defaultBoard = defaultPromotion.execute();
        
        // Verify the promoted piece is a Queen (default behavior)
        Tile promotedTile = defaultBoard.getTile(0);
        assertTrue(promotedTile.isTileOccupied());
        assertEquals(Queen.class, promotedTile.getPiece().getClass());
        assertEquals(Alliance.WHITE, promotedTile.getPiece().getPieceAlliance());
    }
    
    @Test
    void testInvalidPromotionPieceType() {
        // Test that invalid promotion piece types throw an exception
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Pawn whitePawn = new Pawn(8, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn), "test");
        List<Tile> tiles = board.getTiles();
        
        // Test invalid promotion piece type
        assertThrows(IllegalArgumentException.class, () -> {
            new PawnPromotionMove(tiles, 8, 0, whitePawn, "INVALID_PIECE");
        });
    }
}
