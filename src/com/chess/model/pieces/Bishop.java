package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.Alliance;
import com.chess.model.board.BoardUtils;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;


public class Bishop extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS= { -9, -7, 7, 9 };
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.BISHOP, pieceCoordinate, pieceAlliance, true);
	}
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.BISHOP, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
	@Override
	public String toString() {
		return this.pieceSymbol.toString();
	}
	
    @Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles,final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {
		return CalculateMoveUtils1.calculate(boardTiles, this, CANDIDATE_MOVE_OFFSETS, checkingMoves, oppositePlayerMoves);
	} 
    
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Bishop(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
