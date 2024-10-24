package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
	
	private static final int[] CANDIDATE_MOVE_COORDINATES = {-17, -15, -10, -6, 6, 10, 15, 17};
	
	public Knight(final int piecePosition, final Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }
	
	@Override
    public List<Move> calculateLegalMoves(final Board board) {
		
		int candidateDestinationCoordinate;
		final List<Move> legalMoves = new ArrayList<>();
		
		for (final int currentCandidate : CANDIDATE_MOVE_COORDINATES) {
			candidateDestinationCoordinate = this.piecePosition + currentCandidate;
			
            // Check if the candidate tile is within the board boundaries and not occupied by a friendly piece.
            if (board.isValidTile(candidateDestinationCoordinate)) {
            	
            	final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
            	if(!candidateDestinationTile.isTileOccupied()){
            		legalMoves.add(new Move());
            	}else {
            		if(candidateDestinationTile.getPiece().getPieceAlliance()!= this.pieceAlliance){
                        legalMoves.add(new Move());
                    }
            	}
            }
        } 
        return ImmutableList.copyOf(legalMoves);
	}
}
