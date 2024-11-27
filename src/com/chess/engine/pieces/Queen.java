package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.MoveResult;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.google.common.collect.ImmutableList;

public class Queen extends Piece {

	public Queen(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.QUEEN, pieceCoordinate, pieceAlliance, true);
	}
	
	public Queen(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.QUEEN, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
    
	@Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final MoveResult moveResult) {
        final Collection<Move> equivalentRookMoves =  new Rook(this.pieceCoordinate, this.pieceAlliance).calculateMoves(boardTiles,moveResult);
        final Collection<Move> equivalentBishopMoves = new Bishop(this.pieceCoordinate, this.pieceAlliance).calculateMoves(boardTiles,moveResult);
        final List<Move> legalMoves = new ArrayList<>(equivalentRookMoves);
        legalMoves.addAll(equivalentBishopMoves);
        return ImmutableList.copyOf(legalMoves);
	}
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Queen(destinationCoordinate, this.getPieceAlliance());
    }
}