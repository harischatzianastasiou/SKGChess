package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.MoveResult;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

public abstract class Piece {

	protected final PieceSymbol pieceSymbol;
	protected final int pieceCoordinate;
	protected final Alliance pieceAlliance;
	private final boolean isFirstMove;
	private final int cachedHashCode;
	
	Piece(final PieceSymbol pieceSymbol, final int piecePosition,  final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceSymbol = pieceSymbol;
        this.pieceCoordinate = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
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
	
	public abstract Collection<Move> calculateMoves(final List<Tile> boardTiles, final MoveResult moveResult);
	
	public abstract Piece movePiece(int destinationCoordinate);
	
	protected boolean isTileUnderAttack(int coordinate, Collection<Move> opponentMoves) {
	    for (Move move : opponentMoves) {
	        if (move.getTargetCoordinate() == coordinate) {
	            return true;
	        }
	    }
	    return false;
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