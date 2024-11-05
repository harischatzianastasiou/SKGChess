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

public class Rook extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -8, -1, 1, 8 };
	private static final int MAX_SQUARES_MOVED = 7;
	
	Rook(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
	}
	
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {

		final List<Move> legalMoves = new ArrayList<>();
		int coordinateOfAppliedOffset;
		
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				coordinateOfAppliedOffset = this.pieceCoordinate + total_offset;

	            if (BoardUtils.isValidTileCoordinate(coordinateOfAppliedOffset)){
	            	if((Math.abs(coordinateOfAppliedOffset - this.pieceCoordinate)) % 8 == 0 || Math.abs(coordinateOfAppliedOffset/8) == Math.abs(this.pieceCoordinate/8)){ // Check if the tile is on the same rank or file as the source tile.
						
		            	final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
	            	
		            	if(!candidateDestinationTile.isTileOccupied() ) {
		            		legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
		            		System.out.println(coordinateOfAppliedOffset);
		            		
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
		                        legalMoves.add(new CapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this, pieceOnCandidateDestinationTile));
		                        System.out.println(coordinateOfAppliedOffset);
		                    }
		            		break;//if there is a piece in the direction that bishop can move, stop further checking in this direction.
		            	}
	            	}
	            }
			}
		}
		return ImmutableList.copyOf(legalMoves);
    }
}