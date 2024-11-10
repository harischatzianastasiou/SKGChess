package com.chess.engine.pieces;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public abstract class Piece {

	protected final int pieceCoordinate;
	protected final Alliance pieceAlliance;
	
	Piece(final int pieceCoordinate, final Alliance pieceAlliance) {
		this.pieceCoordinate = pieceCoordinate;
        this.pieceAlliance = pieceAlliance;
	}
	
	public int getPieceCoordinate() {
		return this.pieceCoordinate;     
	}
	
	public Alliance getPieceAlliance() {
		return this.pieceAlliance;
	}
	
	public abstract Collection<Move> calculateLegalMoves(final Board board);
	
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