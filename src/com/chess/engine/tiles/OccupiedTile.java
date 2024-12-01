package com.chess.engine.tiles;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;

public final class OccupiedTile extends Tile {
		
		private final Piece pieceOnTile;
		
        private OccupiedTile(int coordinate, final Alliance alliance, final Piece piece) {
            super(coordinate, alliance);
            this.pieceOnTile = piece;
        }

        static OccupiedTile createOccupiedTile(int coordinate, final Alliance alliance, final Piece piece) {
            return new OccupiedTile(coordinate, alliance, piece);
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
