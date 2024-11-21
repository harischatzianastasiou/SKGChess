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
import com.google.common.collect.ImmutableList;

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
	
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.targetCoordinate;
        result = 31 * result + this.pieceToMove.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        final Move otherMove = (Move) other;
        return getSourceCoordinate() == otherMove.getSourceCoordinate() &&
        	   getTargetCoordinate() == otherMove.getTargetCoordinate() &&
               getPieceToMove().equals(otherMove.getPieceToMove());
    }
    
    public List<Tile> getBoardTiles() {
    	return ImmutableList.copyOf(boardTiles);
    }
    
    public int getSourceCoordinate() {
    	return sourceCoordinate;
    }
    
	public int getTargetCoordinate() {
		return targetCoordinate;
	}
	
	public Piece getPieceToMove() {
		return pieceToMove;
	}
	
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
         
         return builder.build(this);
    }
    
    public Board undo() {
        final Board.Builder builder = new Board.Builder();
        for (final Tile tile : this.boardTiles) {
            if (tile.isTileOccupied()) {
                builder.setPiece(tile.getPiece());
            }
        }        
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance());
        return builder.build();
    }
    
	public abstract Piece getCapturedPiece();

	public static class NonCapturingMove extends Move{
        public NonCapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
            super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        }
     
        @Override
        public Piece getCapturedPiece() {
            return null;
        }
	}
	
	public static class CapturingMove extends Move{
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
	
	public static class PawnMove extends NonCapturingMove {
		public PawnMove(final List<Tile> boardTiles,
						final int sourceCoordinate,
		                final int targetCoordinate,
		                final Piece pieceToMove) {
		    super(boardTiles, sourceCoordinate, targetCoordinate,pieceToMove );
		}
		
		@Override
		public boolean equals(final Object other) {
		    return this == other || other instanceof PawnMove && super.equals(other);
		}
	}
	
	public static class PawnJumpMove extends NonCapturingMove {
		public PawnJumpMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
			super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
		}
		@Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnJumpMove && super.equals(other);
        }
	}
	        
	public static class PawnPromotionMove extends NonCapturingMove {
		public PawnPromotionMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
			super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
		}
		@Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof PawnPromotionMove && super.equals(other);
	    }
	}
	    	
	public static class PawnAttackMove extends CapturingMove {
		public PawnAttackMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
		    super(boardTiles, sourceCoordinate, targetCoordinate,pieceToMove,capturedPiece);
		}
		
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnAttackMove && super.equals(other);
        }
	}
	
	public static class PawnEnPassantAttack extends PawnAttackMove {
		public PawnEnPassantAttack(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
			super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove, capturedPiece);
		}
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnEnPassantAttack && super.equals(other);
        }
	}
	
	public static class CastleMove extends NonCapturingMove {
		public CastleMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
			super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
		}
		@Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof CastleMove && super.equals(other);
	    }
	}
	
	public static class KingSideCastleMove extends NonCapturingMove {
		public KingSideCastleMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
			super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
		}
		@Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof KingSideCastleMove && super.equals(other);
	    }
	}
	
	public static class QueenSideCastleMove extends NonCapturingMove {
		public QueenSideCastleMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
			super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
		}
		@Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof QueenSideCastleMove && super.equals(other);
	    }
	}
}

