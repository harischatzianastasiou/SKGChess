package com.chess.engine.board;

import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.Tile.EmptyTile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Rook;

public abstract class Move {
	final List<Tile> boardTiles;
	final int sourceCoordinate;
	final int targetCoordinate;
	private final Piece pieceToMove;// with the alliance of the moved piece we know who made the move
	
	private Move(final List<Tile> boardTiles,final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
	    this.boardTiles = boardTiles;    
		this.sourceCoordinate = sourceCoordinate;
        this.targetCoordinate = targetCoordinate;
        this.pieceToMove = pieceToMove;
	}
	
	public abstract Piece getCapturedPiece();
	
    public Board execute() {
 		// Create a new board builder
         Board.Builder builder = new Board.Builder();
 	        
         for (final Tile tile : this.boardTiles) {
             if (tile.isTileOccupied()) {
                 final Piece piece = tile.getPiece();
                 // Iterate over all current player pieces on the board
             	if (piece.getPieceAlliance() == this.getPieceToMove().getPieceAlliance()) {
             		if(!this.getPieceToMove().equals(piece)) {
             			builder.setPiece(piece);
             		}
             	// Iterate over all opponent pieces on the board
             	}else {
             	    // If the piece is not captured, place it on the new board
                     if (this.getTargetCoordinate() != piece.getPieceCoordinate()) {
                         builder.setPiece(piece);
                     }
             	}
             }
         }

         // Create the moved piece on the new board
         Piece movedPiece = this.getPieceToMove().movePiece(targetCoordinate);
         builder.setPiece(movedPiece);
 	        
         // Set the next player's alliance
         builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
         // Build and return the new board
         return builder.build();
    }
    
	
	public int getTargetCoordinate() {
		return targetCoordinate;
	}
	
	public Piece getPieceToMove() {
		return pieceToMove;
	}

	public static final class NonCapturingMove extends Move{
        public NonCapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
            super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        }
     
        @Override
        public Piece getCapturedPiece() {
            return null;
        }
	}
	
	public static final class CapturingMove extends Move{
		private final Piece capturedPiece;
		public CapturingMove(final List<Tile> boardTiles,final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
            super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
            this.capturedPiece = capturedPiece;
		}
		
		@Override
		public Piece getCapturedPiece() {
            return this.capturedPiece;
		}
	}
	
	
}

