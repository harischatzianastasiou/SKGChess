package com.chess.engine.pieces;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;

public abstract class Piece {

	protected final Tile pieceTile;
	protected final Alliance pieceAlliance;
	
	Piece(final Tile pieceTile, final Alliance pieceAlliance) {
		this.pieceTile = pieceTile;
        this.pieceAlliance = pieceAlliance;
	}
	
	public Alliance getPieceAlliance() {
		return this.pieceAlliance;
	}
	
	public abstract Collection<Move> calculateLegalMoves(final Board board);
}