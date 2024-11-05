package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.*;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = {-17, -15, -10, -6, 6, 10, 15, 17};
	
	public Knight(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
    }
	
	@Override
    public Collection<Move> calculateLegalMoves(final Board board) {
		
		final List<Move> legalMoves = new ArrayList<>();
		int coordinateOfAppliedOffset;
		
	    // Iterate over all possible L-shaped moves for a knight
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			coordinateOfAppliedOffset = this.pieceCoordinate + candidateOffset;
			
            if (BoardUtils.isValidTileCoordinate(coordinateOfAppliedOffset)) {
            	final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
            	final Tile currentTile = board.getTile(pieceCoordinate);
            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
            	
            	if (allianceOfCandidateDestinationTile != allianceOfCurrentTile) {
            		
	            	if(!candidateDestinationTile.isTileOccupied()) {
	            		legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
	            		System.out.println(coordinateOfAppliedOffset);
	            		
	            	}else {
	 
	            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
	            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
	            		
	            		if(this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
	                        legalMoves.add(new CapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this, pieceOnCandidateDestinationTile));
	                        System.out.println(coordinateOfAppliedOffset);
	                    }
	            	}
            	}
            }
        } 
        return ImmutableList.copyOf(legalMoves);
	}
}