package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

public class Queen extends Piece {

	public Queen(final Tile pieceTile, final Alliance pieceAlliance ) {
		super(pieceTile, pieceAlliance);
	}
	
	public Collection<Move> calculateLegalMoves(final Board board) {
	       
        final Collection<Move> equivalentRookMoves =  new Rook(this.pieceTile, this.pieceAlliance).calculateLegalMoves(board);
        final Collection<Move> equivalentBishopMoves = new Bishop(this.pieceTile, this.pieceAlliance).calculateLegalMoves(board);
        final List<Move> legalMoves = new ArrayList<>(equivalentRookMoves);
        legalMoves.addAll(equivalentBishopMoves);

        return ImmutableList.copyOf(legalMoves);
	}
}
