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
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.google.common.collect.ImmutableList;

public class King extends Piece  {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -9, -8, -7, -1, 1, 7, 8, 9 };
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
    }
	
	@Override
	public String toString() {
		return PieceSymbol.KING.toString();
	}

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
		final List<Move> legalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
				candidateDestinationCoordinate = this.pieceCoordinate + candidateOffset;
	            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
	            	final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
	            	int rankDifference = Math.abs(BoardUtils.getCoordinateRankDifference(candidateDestinationCoordinate,this.pieceCoordinate));
	                int fileDifference = Math.abs(BoardUtils.getCoordinateFileDifference(candidateDestinationCoordinate,this.pieceCoordinate));
	                if (rankDifference <= 1 && fileDifference <= 1) {
	                	if(!candidateDestinationTile.isTileOccupied() ) {
		            		legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, candidateDestinationCoordinate, this));
//		            		System.out.println(candidateDestinationCoordinate);	
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
		                        legalMoves.add(new CapturingMove(board, this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
//		                        System.out.println(candidateDestinationCoordinate);
		                    }
		            		break;//if there is a piece in the direction that king can move, stop further checking in this direction.
		            	}
	            	}
	            }
		}
		return ImmutableList.copyOf(legalMoves);
    }
}
