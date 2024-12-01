package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.moves.Move;
import com.chess.engine.board.moves.capturingMoves.CapturingMove;
import com.chess.engine.board.moves.nonCapturingMoves.NonCapturingMove;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

public class Knight extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = {-17, -15, -10, -6, 6, 10, 15, 17};
	
	public Knight(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.KNIGHT, pieceCoordinate, pieceAlliance, true);
	}
	
	public Knight(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.KNIGHT, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
	
	@Override
	public Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles) {
		final List<Move> knightPotentialLegalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
	    // Iterate over all possible L-shaped moves for a knight
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			candidateDestinationCoordinate = this.pieceCoordinate + candidateOffset;
            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
            	final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
            	final Tile currentTile = boardTiles.get(pieceCoordinate);
            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance();
            	if (allianceOfCandidateDestinationTile != allianceOfCurrentTile) {
            		if (!candidateDestinationTile.isTileOccupied()) {
            		    knightPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
	            	}else {
	            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
	            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
	            		if(this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
	                        knightPotentialLegalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
	                    }
	            	}
            	}
            }
        } 
        return ImmutableList.copyOf(knightPotentialLegalMoves);
	}
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Knight(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
