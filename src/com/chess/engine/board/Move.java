package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.board.Tile.EmptyTile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Piece;

public abstract class Move {

	final Board board;
	final Tile sourceTile;
	final Tile targetTile;
	final Piece movedPiece;
	
	
	private Move(final Board board, final Tile sourceTile, final Tile targetTile, final Piece movedPiece) {
			this.board = board;
	        this.sourceTile = sourceTile;
	        this.targetTile = targetTile;
	        this.movedPiece = movedPiece;
	}
	
	public abstract Piece getCapturedPiece();
	
	public static final class NonCapturingMove extends Move{
		
        public NonCapturingMove(final Board board, final Tile sourceTile, final Tile targetTile, final Piece movedPiece) {
            super(board, sourceTile, targetTile, movedPiece);
        }
        
        @Override
        public Piece getCapturedPiece() {
            return null;
        }
	}
	
	public static final class CapturingMove extends Move{ 
		
		private final Piece capturedPiece;
		
		public CapturingMove(final Board board, final Tile sourceTile, final Tile targetTile, final Piece movedPiece, final Piece capturedPiece) {
            super(board, sourceTile, targetTile, movedPiece);
            this.capturedPiece = capturedPiece;
		}
		
		@Override
		public Piece getCapturedPiece() {
            return this.capturedPiece;
        }
	}
}
