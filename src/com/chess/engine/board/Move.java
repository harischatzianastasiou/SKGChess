package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.Tile.EmptyTile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Piece;

public abstract class Move {
	final int sourceCoordinate;
	final int targetCoordinate;
	private final Piece movedPiece;// with the alliance of the moved piece we know who made the move
	
	private Move(final int sourceCoordinate, final int targetCoordinate, final Piece movedPiece) {
	        this.sourceCoordinate = sourceCoordinate;
	        this.targetCoordinate = targetCoordinate;
	        this.movedPiece = movedPiece;
	}
	
	public abstract Piece getCapturedPiece();
	
	public int getTargetCoordinate() {
		return targetCoordinate;
	}
	
	public Piece getMovedPiece() {
		return movedPiece;
	}

	public static final class NonCapturingMove extends Move{
        public NonCapturingMove(final int sourceCoordinate, final int targetCoordinate, final Piece movedPiece) {
            super(sourceCoordinate, targetCoordinate, movedPiece);
        }
     
        @Override
        public Piece getCapturedPiece() {
            return null;
        }
	}
	
	public static final class CapturingMove extends Move{
		private final Piece capturedPiece;
		public CapturingMove(final int sourceCoordinate, final int targetCoordinate, final Piece movedPiece, final Piece capturedPiece) {
            super(sourceCoordinate, targetCoordinate, movedPiece);
            this.capturedPiece = capturedPiece;
		}
		
		@Override
		public Piece getCapturedPiece() {
            return this.capturedPiece;
        }
	}
}

