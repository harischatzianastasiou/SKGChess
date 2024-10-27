package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
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
		
		int candidateDestinationCoordinate;
		final List<Move> legalMoves = new ArrayList<>();
		
		for (final int currentCandidate : CANDIDATE_MOVE_COORDINATES) {
			candidateDestinationCoordinate = this.pieceTile.getTileCoordinate() + currentCandidate;
			
            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
            	
            	final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
            	
            	// In chess a Knight can move to a square that is not of the same color.
            	if (candidateDestinationTile.getTileAlliance()!= this.pieceTile.getTileAlliance()) {
            		
	            	if(!candidateDestinationTile.isTileOccupied() ) {
	            		legalMoves.add(new Move());
	            		System.out.println(candidateDestinationCoordinate);
	            	}else {
	            		if(candidateDestinationTile.getPiece().getPieceAlliance()!= this.pieceAlliance){
	                        legalMoves.add(new Move());
	                        System.out.println(candidateDestinationCoordinate);
	                    }
	            	}
            	}
            }
            
        } 
        return ImmutableList.copyOf(legalMoves);
	}
}
