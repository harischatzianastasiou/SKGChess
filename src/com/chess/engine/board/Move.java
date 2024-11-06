package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.Tile.EmptyTile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Piece;

public abstract class Move {

	final Board board;
	final int sourceCoordinate;
	final int targetCoordinate;
	final Piece movedPiece;
	
	private Move(final Board board, final int sourceCoordinate, final int targetCoordinate, final Piece movedPiece) {
			this.board = board;
	        this.sourceCoordinate = sourceCoordinate;
	        this.targetCoordinate = targetCoordinate;
	        this.movedPiece = movedPiece;
	}
	
	public abstract Piece getCapturedPiece();
	
	public static final class NonCapturingMove extends Move{
        public NonCapturingMove(final Board board, final int sourceCoordinate, final int targetCoordinate, final Piece movedPiece) {
            super(board, sourceCoordinate, targetCoordinate, movedPiece);
        }
     
        @Override
        public Piece getCapturedPiece() {
            return null;
        }
	}
	
	public static final class CapturingMove extends Move{
		private final Piece capturedPiece;
		public CapturingMove(final Board board, final int sourceCoordinate, final int targetCoordinate, final Piece movedPiece, final Piece capturedPiece) {
            super(board, sourceCoordinate, targetCoordinate, movedPiece);
            this.capturedPiece = capturedPiece;
		}
		
		@Override
		public Piece getCapturedPiece() {
            return this.capturedPiece;
        }
	}
}

