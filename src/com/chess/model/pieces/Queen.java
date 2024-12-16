package com.chess.model.pieces;

import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;

public class Queen extends Piece {
    
    private static final int[] QUEEN_MOVE_OFFSETS = { -8, -1, 1, 8, -9, -7, 7, 9 };

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
	public Collection<Move> calculateMoves(final List<Tile> boardTiles,final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {
		return CalculateMoveUtils.calculate(boardTiles, this, QUEEN_MOVE_OFFSETS, checkingMoves, oppositePlayerMoves);
	} 

    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Queen(destinationCoordinate, this.getPieceAlliance(), false);
    }
}
