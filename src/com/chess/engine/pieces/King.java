package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Move.CapturingMove;
import com.chess.engine.board.Move.NonCapturingMove;
import com.google.common.collect.ImmutableList;

public class King extends Piece  {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -9, -8, -7, -1, 1, 7, 8, 9 };
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
		final List<Move> legalMoves = new ArrayList<>();
		int coordinateOfAppliedOffset;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
				coordinateOfAppliedOffset = this.pieceCoordinate + candidateOffset;
				
	            if (BoardUtils.isValidTileCoordinate(coordinateOfAppliedOffset)){
	            	final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
	            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
	            	final Tile currentTile = board.getTile(pieceCoordinate);
	            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
	            	if(Math.abs(BoardUtils.getTileCoordinateRank(coordinateOfAppliedOffset) - BoardUtils.getTileCoordinateRank(this.pieceCoordinate)) <= 1 &&
	            			Math.abs(BoardUtils.getTileCoordinateFile(coordinateOfAppliedOffset) - BoardUtils.getTileCoordinateFile(this.pieceCoordinate)) <= 1) {
	            			if(!candidateDestinationTile.isTileOccupied() ) {
		            		legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
		            		System.out.println(coordinateOfAppliedOffset);	
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
		                        legalMoves.add(new CapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this, pieceOnCandidateDestinationTile));
		                        System.out.println(coordinateOfAppliedOffset);
		                    }
		            		break;//if there is a piece in the direction that king can move, stop further checking in this direction.
		            	}
	            	}
	            }
		}
		return ImmutableList.copyOf(legalMoves);
    }
}
