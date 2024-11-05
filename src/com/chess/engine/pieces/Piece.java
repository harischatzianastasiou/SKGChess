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
	
	public Alliance getPieceAlliance() {
		return this.pieceAlliance;
	}
	
	public abstract Collection<Move> calculateLegalMoves(final Board board);
}