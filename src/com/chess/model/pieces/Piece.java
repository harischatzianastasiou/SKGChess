package com.chess.model.pieces;

import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;

public abstract class Piece {

	protected final PieceSymbol pieceSymbol;
	protected final int pieceCoordinate;
	protected final Alliance pieceAlliance;
	private final boolean isFirstMove;
	private final int cachedHashCode;
	
	Piece(final PieceSymbol pieceSymbol, final int pieceCoordinate,  final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceSymbol = pieceSymbol;
        this.pieceCoordinate = pieceCoordinate;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

	@Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }
        final Piece otherPiece = (Piece) other;
        return this.pieceCoordinate == otherPiece.pieceCoordinate && this.pieceSymbol == otherPiece.pieceSymbol &&
               this.pieceAlliance == otherPiece.pieceAlliance && this.isFirstMove == otherPiece.isFirstMove;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    private int computeHashCode() {
        int result = this.pieceSymbol.hashCode();
        result = 31 * result + this.pieceAlliance.hashCode();
        result = 31 * result + this.pieceCoordinate;
        result = 31 * result + (this.isFirstMove ? 1 : 0);
        return result;
    }
	
	public PieceSymbol getPieceSymbol() {
		return pieceSymbol;
	}
	
	public int getPieceCoordinate() {
		return this.pieceCoordinate;     
	}
	
	public Alliance getPieceAlliance() {
		return this.pieceAlliance;
	}
	
	public boolean isFirstMove() {
		return isFirstMove;
	}
	
	public abstract Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles);
	
	public abstract Piece movePiece(int destinationCoordinate);
	
	protected boolean isTileUnderAttack(int coordinate, Collection<Move> opponentMoves) {
	    for (Move move : opponentMoves) {
	        if (move.getTargetCoordinate() == coordinate) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public enum PieceSymbol {
		PAWN("P"), 
		KNIGHT("N"),
		BISHOP("B"), 
		ROOK("R"), 
		QUEEN("Q"), 
		KING("K");
        
        private final String symbol;
        
        PieceSymbol(final String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return this.symbol;
        }
	}
}
