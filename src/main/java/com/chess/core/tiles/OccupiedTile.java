package  com.chess.core.tiles;

import  com.chess.core.Alliance;
import  com.chess.core.pieces.Piece;

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
        public Piece getPiece() {
            return this.pieceOnTile;
        }

        @Override
        public boolean isTileOccupied() {
            return true;
        }
	}
