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
	
	private static final int BISHOP_MAX_DIAGONAL_DISTANCE = 7;
	private static final int[] CANDIDATE_MOVE_VECTOR_COORDINATES = { -9, -7, 7, 9 };
	
	public Bishop(final Tile pieceTile, final Alliance pieceAlliance) {
        super(pieceTile, pieceAlliance);
	}
	
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
		
		int candidateTargetCoordinate;
		final List<Move> legalMoves = new ArrayList<>();
		
			for (final int currentMoveCandidate : CANDIDATE_MOVE_VECTOR_COORDINATES) {
				
				for(int distance=1; distance <= BISHOP_MAX_DIAGONAL_DISTANCE; distance++ ) {
				candidateTargetCoordinate = this.pieceTile.getTileCoordinate() + (currentMoveCandidate * distance);
				
	            if (BoardUtils.isValidTileCoordinate(candidateTargetCoordinate)) {
	            	
	            	final Tile candidateTargetTile = board.getTile(candidateTargetCoordinate);
	            	final Alliance candidateTargetTileAlliance = candidateTargetTile.getTileAlliance();
	            	final Alliance sourceTileAlliance = this.pieceTile.getTileAlliance(); 
	            	
	            	if (candidateTargetTileAlliance == sourceTileAlliance) {
	            		
		            	if(!candidateTargetTile.isTileOccupied() ) {
		            		
		            		legalMoves.add(new NonCapturingMove(board, this.pieceTile, candidateTargetTile, this));
		            		System.out.println(candidateTargetCoordinate);
		            		
		            	}else {
		            		
		            		final Piece targetPiece = candidateTargetTile.getPiece();
		            		final Alliance targetPieceAlliance = targetPiece.getPieceAlliance();
		            		
		            		if( this.pieceAlliance != targetPieceAlliance ){
		            			
		                        legalMoves.add(new CapturingMove(board, this.pieceTile, targetPiece.pieceTile, this, targetPiece));
		                        System.out.println(candidateTargetCoordinate);
		                    }
		            		break;//if there is a piece in the direction that bishop can move, stop further checking in this direction.
		            	}
	            	}
	            	else {
	            		break; //if the current vector does not apply for the pieceTile(eg for tiles that are on 1st or 8th rank), stop further checking in this direction.
	            	}
	            }
	            else {
	            	break;//If the candidateTargetCoordinate is out of boundaries, stop further checking in this direction.
	            }
	        } 
		
		}
        return ImmutableList.copyOf(legalMoves);
    }
}
