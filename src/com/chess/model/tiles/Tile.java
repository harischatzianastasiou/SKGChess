package com.chess.model.tiles;

import java.util.Map;

import com.chess.model.Alliance;
import com.chess.model.pieces.Piece;

public abstract class Tile {                                                                       

	protected final int tileCoordinate;
	protected final Alliance tileAlliance;	
	private static final Map<Integer, Tile> EMPTY_TILES_CACHE = EmptyTile.createEmptyTilesCache();

	public static Tile createTile(final int tileCoordinate,final Alliance alliance, final Piece piece) {
		return piece != null? OccupiedTile.createOccupiedTile(tileCoordinate, alliance, piece) : EMPTY_TILES_CACHE.get(tileCoordinate);
	}
	
	protected Tile(final int tileCoordinate,  final Alliance tileAlliance) {
		this.tileCoordinate = tileCoordinate;
		this.tileAlliance = tileAlliance;
	}
	
    public int getTileCoordinate() {
		return this.tileCoordinate;
	}

	public Alliance getTileAlliance() {
		return this.tileAlliance;
	}
	
	public abstract Piece getPiece();

	public abstract boolean isTileOccupied();
}