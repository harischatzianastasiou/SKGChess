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
	
	private static final int[] CANDIDATE_MOVE_COORDINATES = {-17, -15, -10, -6, 6, 10, 15, 17};
	
	public Knight(final Tile pieceTile, final Alliance pieceAlliance) {
        super(pieceTile, pieceAlliance);
    }
	
	@Override
    public Collection<Move> calculateLegalMoves(final Board board) {
		
		int candidateTargetCoordinate;
		final List<Move> legalMoves = new ArrayList<>();
		
		for (final int currentMoveCandidate : CANDIDATE_MOVE_COORDINATES) {
			candidateTargetCoordinate = this.pieceTile.getTileCoordinate() + currentMoveCandidate;
			
            if (BoardUtils.isValidTileCoordinate(candidateTargetCoordinate)) {
            	
            	final Tile candidateTargetTile = board.getTile(candidateTargetCoordinate);
            	final Alliance candidateTargetTileAlliance = candidateTargetTile.getTileAlliance();
            	final Alliance sourceTileAlliance = this.pieceTile.getTileAlliance(); 
            	
            	if (candidateTargetTileAlliance != sourceTileAlliance) {
            		
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
	            	}
            	}
            }
            
        } 
        return ImmutableList.copyOf(legalMoves);
	}
}
