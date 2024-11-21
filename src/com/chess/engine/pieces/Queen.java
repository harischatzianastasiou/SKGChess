package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.google.common.collect.ImmutableList;

public class Queen extends Piece {

	public Queen(final int pieceCoordinate, final Alliance pieceAlliance ) {
		super(pieceCoordinate, pieceAlliance);
	}
	
	@Override
	public String toString() {
		return PieceSymbol.QUEEN.toString();
	}
	
	@Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles) {
        final Collection<Move> equivalentRookMoves =  new Rook(this.pieceCoordinate, this.pieceAlliance).calculateMoves(boardTiles);
        final Collection<Move> equivalentBishopMoves = new Bishop(this.pieceCoordinate, this.pieceAlliance).calculateMoves(boardTiles);
        final List<Move> legalMoves = new ArrayList<>(equivalentRookMoves);
        legalMoves.addAll(equivalentBishopMoves);
        return ImmutableList.copyOf(legalMoves);
	}
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Queen(destinationCoordinate, this.getPieceAlliance());
    }
}
