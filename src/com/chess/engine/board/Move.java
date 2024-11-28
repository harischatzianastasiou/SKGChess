package com.chess.engine.board;

import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board.Builder;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

public abstract class Move {
	final List<Tile> boardTiles;
	final int sourceCoordinate;
	final int targetCoordinate;
	private final Piece pieceToMove;// with the alliance of the moved piece we know who made the move
	
	private Move(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
	    this.boardTiles = boardTiles;    
		this.sourceCoordinate = sourceCoordinate;
        this.targetCoordinate = targetCoordinate;
        this.pieceToMove = pieceToMove;
	}
	
	public abstract MoveResult simulate();
	
    public abstract Board execute();	
    
    public Builder createBuilderAfterCapturingMove() {
    	// Create a new board builder
        Board.Builder builder = new Board.Builder();
	        
        for (final Tile tile : this.boardTiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                // Iterate over all current player pieces on the board
            		if(!this.getPieceToMove().equals(piece)) {
            			builder.setPiece(piece);
            		}
            }   
        }

        // Create the moved piece on the new board
        Piece movedPiece = this.getPieceToMove().movePiece(targetCoordinate);
        builder.setPiece(movedPiece);
	        
        // Set the next player's alliance
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
        return builder;
    }
    
    public Builder createBuilderAfterNonCapturingMove() {
    	// Create a new board builder
        Board.Builder builder = new Board.Builder();
	        
        for (final Tile tile : this.boardTiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                // Iterate over all current player pieces on the board
            		if(!this.getPieceToMove().equals(piece)) {
            			builder.setPiece(piece);
            		}
            }   
        }

        // Create the moved piece on the new board
        Piece movedPiece = this.getPieceToMove().movePiece(targetCoordinate);
        builder.setPiece(movedPiece);
	        
        // Set the next player's alliance
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
        return builder;
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
        public MoveResult simulate() {
        	return MoveResult.create(this,super.createBuilderAfterNonCapturingMove().build());
        }
        
        @Override
        public Board execute() {
        	 return super.createBuilderAfterNonCapturingMove().build(this);
        }
        
        @Override
        public Piece getCapturedPiece() {
            return null;
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
			// add logic to promote pawn
		}
		
        @Override
        public MoveResult simulate() {
        	// Create a new board builder
	        Board.Builder builder = new Board.Builder();

	        for (final Tile tile : this.boardTiles) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                // Iterate over all current player pieces on the board
	                if (!this.getPieceToMove().equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Let the user select a new piece
	        System.out.println("Select a new piece for the pawn (Queen, Rook, Bishop, Knight): ");
	        // Assume user input is handled elsewhere and stored in newPieceType
	        String newPieceType = "QUEEN"; // Replace with actual user input

	        // Create the promoted piece on the new board
	        Piece promotedPiece = ((Pawn) this.getPieceToMove()).promotePawn(targetCoordinate, newPieceType);
	        builder.setPiece(promotedPiece);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);

	        return MoveResult.create(this,builder.build());
        }
        
		@Override
        public Board execute() {
	        // Create a new board builder
	        Board.Builder builder = new Board.Builder();

	        for (final Tile tile : this.boardTiles) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                // Iterate over all current player pieces on the board
	                if (!this.getPieceToMove().equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Let the user select a new piece
	        System.out.println("Select a new piece for the pawn (Queen, Rook, Bishop, Knight): ");
	        // Assume user input is handled elsewhere and stored in newPieceType
	        String newPieceType = "QUEEN"; // Replace with actual user input

	        // Create the promoted piece on the new board
	        Piece promotedPiece = ((Pawn) this.getPieceToMove()).promotePawn(targetCoordinate, newPieceType);
	        builder.setPiece(promotedPiece);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);

	        return builder.build(this);
	    }
		@Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof PawnPromotionMove && super.equals(other);
	    }
	}
	
	public static class CapturingMove extends Move{
		private final Piece capturedPiece;

		public CapturingMove(final List<Tile> boardTiles,final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
            super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
            this.capturedPiece = capturedPiece;
		}
        @Override
        public MoveResult simulate() {
        	return MoveResult.create(this,super.createBuilderAfterCapturingMove().build());
        }
        
        @Override
        public Board execute() {
        	return super.createBuilderAfterCapturingMove().build(this);
	    }
		
		@Override
		public Piece getCapturedPiece() {
            return this.capturedPiece;
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
	
	public static class KingSideCastleMove extends NonCapturingMove {
	    protected final int rookSourceCoordinate;
	    protected final int rookTargetCoordinate;
	    protected final Rook rook;

	    public KingSideCastleMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final int rookSourceCoordinate, final int rookTargetCoordinate, final Rook rook) {
	        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
	        this.rookSourceCoordinate = rookSourceCoordinate;
	        this.rookTargetCoordinate = rookTargetCoordinate;
	        this.rook = rook;
	    }
	    
	    @Override
        public MoveResult simulate() {
	        final Board.Builder builder = new Board.Builder();
	        
	        for (final Tile tile : this.boardTiles) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                if (!this.getPieceToMove().equals(piece) && !this.rook.equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Move the king
	        final Piece movedKing = this.getPieceToMove().movePiece(this.getTargetCoordinate());
	        builder.setPiece(movedKing);

	        // Move the rook
	        final Piece movedRook = this.rook.movePiece(this.rookTargetCoordinate);
	        builder.setPiece(movedRook);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
	        
	        return MoveResult.create(this,builder.build());
	        }
	    
	    @Override
        public Board execute() {
	        final Board.Builder builder = new Board.Builder();
	        
	        for (final Tile tile : this.boardTiles) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                if (!this.getPieceToMove().equals(piece) && !this.rook.equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Move the king
	        final Piece movedKing = this.getPieceToMove().movePiece(this.getTargetCoordinate());
	        builder.setPiece(movedKing);

	        // Move the rook
	        final Piece movedRook = this.rook.movePiece(this.rookTargetCoordinate);
	        builder.setPiece(movedRook);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
	        
	        return builder.build(this); 
	    }

	    @Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof KingSideCastleMove && super.equals(other);
	    }
	}

	public static class QueenSideCastleMove extends NonCapturingMove {
	    protected final int rookSourceCoordinate;
	    protected final int rookTargetCoordinate;
	    protected final Rook rook;

	    public QueenSideCastleMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final int rookSourceCoordinate, final int rookTargetCoordinate, final Rook rook) {
	        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
	        this.rookSourceCoordinate = rookSourceCoordinate;
	        this.rookTargetCoordinate = rookTargetCoordinate;
	        this.rook = rook;
	    }
	    
	    @Override
	    public MoveResult simulate() {
	        final Board.Builder builder = new Board.Builder();
	        
	        for (final Tile tile : this.boardTiles) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                if (!this.getPieceToMove().equals(piece) && !this.rook.equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Move the king
	        final Piece movedKing = this.getPieceToMove().movePiece(this.getTargetCoordinate());
	        builder.setPiece(movedKing);

	        // Move the rook
	        final Piece movedRook = this.rook.movePiece(this.rookTargetCoordinate);
	        builder.setPiece(movedRook);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
	        
	        return MoveResult.create(this,builder.build());
	    }
	    
	    @Override
	    public Board execute() {
	        final Board.Builder builder = new Board.Builder();
	        
	        for (final Tile tile : this.boardTiles) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                if (!this.getPieceToMove().equals(piece) && !this.rook.equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Move the king
	        final Piece movedKing = this.getPieceToMove().movePiece(this.getTargetCoordinate());
	        builder.setPiece(movedKing);

	        // Move the rook
	        final Piece movedRook = this.rook.movePiece(this.rookTargetCoordinate);
	        builder.setPiece(movedRook);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
	        
	        return builder.build(this);
	    }

	    @Override
	    public boolean equals(final Object other) {
	        return this == other || other instanceof QueenSideCastleMove && super.equals(other);
	    }
	}
}

