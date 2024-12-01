package com.chess.engine.board.tiles;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableMap;

public final class EmptyTile extends Tile  { 

    static Map<Integer, Tile> createEmptyTilesCache() {
        final Map<Integer, Tile> emptyTiles = new HashMap<>();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            emptyTiles.put(i, new EmptyTile(i, BoardUtils.getCoordinateAlliance(i)));
        }
        return ImmutableMap.copyOf(emptyTiles);
	}	

    private EmptyTile(final int coordinate, final Alliance alliance) {
        super(coordinate, alliance);
    }
    
    @Override
    public boolean isTileOccupied() {
        return false;
    }
    
    @Override
    public int getTileCoordinate() {
        return this.tileCoordinate;
    }
    
    @Override
    public Alliance getTileAlliance() {
        return this.tileAlliance;
    }	
    
    @Override
    public Piece getPiece() {
        return null;
    }
}
