package com.chess.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.chess.model.Alliance;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Rook;
import com.chess.model.tiles.Tile;

class TileTest {
    
    @Test
    void testEmptyTileCreation() {
        Tile emptyTile = Tile.createTile(0, Alliance.WHITE, null);
        assertNotNull(emptyTile);
        assertFalse(emptyTile.isTileOccupied());
        assertNull(emptyTile.getPiece());
        assertEquals(0, emptyTile.getTileCoordinate());
        assertEquals(Alliance.WHITE, emptyTile.getTileAlliance());
    }
    
    @Test
    void testOccupiedTileCreation() {
        Piece rook = new Rook(0, Alliance.WHITE, true);
        Tile occupiedTile = Tile.createTile(0, Alliance.WHITE, rook);
        
        assertNotNull(occupiedTile);
        assertTrue(occupiedTile.isTileOccupied());
        assertNotNull(occupiedTile.getPiece());
        assertEquals(rook, occupiedTile.getPiece());
        assertEquals(0, occupiedTile.getTileCoordinate());
        assertEquals(Alliance.WHITE, occupiedTile.getTileAlliance());
    }
    
    @Test
    void testEmptyTilesCacheReuse() {
        Tile emptyTile1 = Tile.createTile(0, Alliance.WHITE, null);
        Tile emptyTile2 = Tile.createTile(0, Alliance.WHITE, null);
        // Empty tiles with same coordinate should return the same cached instance
        assertSame(emptyTile1, emptyTile2);
    }
    
    @Test
    void testOccupiedTilesAreNotCached() {
        Piece rook1 = new Rook(0, Alliance.WHITE, true);
        Piece rook2 = new Rook(0, Alliance.WHITE, true);
        Tile occupiedTile1 = Tile.createTile(0, Alliance.WHITE, rook1);
        Tile occupiedTile2 = Tile.createTile(0, Alliance.WHITE, rook2);
        // Occupied tiles should be different instances even with same coordinates
        assertNotSame(occupiedTile1, occupiedTile2);
    }
} 