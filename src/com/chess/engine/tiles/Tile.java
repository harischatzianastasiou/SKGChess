package com.chess.engine.tiles;

import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;

public abstract class Tile {                                                                       

	protected final int tileCoordinate;
	protected final Alliance tileAlliance;	
	private static final Map<Integer, Tile> EMPTY_TILES_CACHE = EmptyTile.createEmptyTilesCache();

	public static Tile createTile(final int tileCoordinate,final Alliance alliance, final Piece piece) {
		return piece != null? OccupiedTile.createOccupiedTile(tileCoordinate, alliance, piece) : EMPTY_TILES_CACHE.get(tileCoordinate);
	}
	
	Tile(final int tileCoordinate,  final Alliance tileAlliance) {
		this.tileCoordinate = tileCoordinate;
		this.tileAlliance = tileAlliance;
	}
	
	public abstract int getTileCoordinate();
	
	public abstract Alliance getTileAlliance();
	
	public abstract Piece getPiece();

	public abstract boolean isTileOccupied();
}