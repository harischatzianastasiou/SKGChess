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

public class Bishop extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS= { -9, -7, 7, 9 };
	private static final int MAX_SQUARES_MOVED = 7;
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
	}
	
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
		final List<Move> legalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
				if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
	            	final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
	            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
	            	final Tile currentTile = board.getTile(pieceCoordinate);
	            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
	            	if (allianceOfCandidateDestinationTile == allianceOfCurrentTile) {
		            	if(!candidateDestinationTile.isTileOccupied()) {
		            		legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, candidateDestinationCoordinate, this));
		            		System.out.println(candidateDestinationCoordinate);
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
		                        legalMoves.add(new CapturingMove(board, this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
		                        System.out.println(candidateDestinationCoordinate);
		                    }
		            		break;//if there is a piece in the direction that bishop can move, stop further checking in this direction.
		            	}
	            	}
	            	else break; //if the current vector does not apply for the pieceTile(eg for tiles that are on 1st or 8th rank), stop further checking in this direction.
	            }
	            else break;//If the candidateTargetCoordinate is out of boundaries, stop further checking in this direction.
			} 
		}
		return ImmutableList.copyOf(legalMoves);
    }
}