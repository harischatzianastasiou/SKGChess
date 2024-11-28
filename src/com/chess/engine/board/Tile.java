package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableMap;

//immutable class
public abstract class Tile {                                                                       

	protected final int tileCoordinate;
	protected final Alliance tileAlliance;	
	private static final Map<Integer, Tile> EMPTY_TILES_CACHE = createEmptyTilesCache();

	private static Map<Integer, Tile> createEmptyTilesCache() {
		final Map<Integer, Tile> emptyTiles = new HashMap<>();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            emptyTiles.put(i, new EmptyTile(i, BoardUtils.getCoordinateAlliance(i)));
        }
        return ImmutableMap.copyOf(emptyTiles);
	}
	// factory method, used for a single point of creation for tiles
	public static Tile createTile(final int tileCoordinate,final Alliance alliance, final Piece piece) {
		return piece != null? new OccupiedTile(tileCoordinate, alliance, piece) : EMPTY_TILES_CACHE.get(tileCoordinate);
	}
	
	private Tile(final int tileCoordinate,  final Alliance tileAlliance) {
		this.tileCoordinate = tileCoordinate;
		this.tileAlliance = tileAlliance;
		
	}
	
	public abstract boolean isTileOccupied();
	
	public abstract int getTileCoordinate();
	
	public abstract Alliance getTileAlliance();
	
	public abstract Piece getPiece();
	
	public static final class EmptyTile extends Tile  { 
		
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
	
	public static final class OccupiedTile extends Tile {
		
		private final Piece pieceOnTile;
		
        private OccupiedTile(int coordinate, final Alliance alliance, final Piece piece) {//private or public, since the constructor of Tile is private then it cannot be instatiated from outside the Tile class.
            super(coordinate, alliance);
            this.pieceOnTile = piece;
        }
        
        @Override
        public boolean isTileOccupied() {
            return true;
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
            return this.pieceOnTile;
        }
	}
}