package com.chess.model.tiles;

import java.util.HashMap;
import java.util.Map;

import com.chess.model.Alliance;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Piece;
import com.google.common.collect.ImmutableMap;

public final class EmptyTile extends Tile  { 

    static Map<Integer, Tile> createEmptyTilesCache() {
        final Map<Integer, Tile> emptyTiles = new HashMap<>();
        for (int i = 0; i < CalculateMoveUtils.NUM_TILES; i++) {
            emptyTiles.put(i, new EmptyTile(i, CalculateMoveUtils.getCoordinateAlliance(i)));
        }
        return ImmutableMap.copyOf(emptyTiles);
	}	

    private EmptyTile(final int coordinate, final Alliance alliance) {
        super(coordinate, alliance);
    }
    
    @Override
    public Piece getPiece() {
        return null;
    }
    
    @Override
    public boolean isTileOccupied() {
        return false;
    }
    

}
