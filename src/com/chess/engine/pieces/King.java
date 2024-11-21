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
        super(PieceSymbol.KING, pieceCoordinate, pieceAlliance, true);
	}
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.KING, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }

    @Override
    public Collection<Move> calculateMoves(final List<Tile> boardTiles) {
		final List<Move> legalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
				candidateDestinationCoordinate = this.pieceCoordinate + candidateOffset;
	            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
	            	final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
	            	int rankDifference = Math.abs(BoardUtils.getCoordinateRankDifference(candidateDestinationCoordinate,this.pieceCoordinate));
	                int fileDifference = Math.abs(BoardUtils.getCoordinateFileDifference(candidateDestinationCoordinate,this.pieceCoordinate));
	                if (rankDifference <= 1 && fileDifference <= 1) {
	                	if(!candidateDestinationTile.isTileOccupied() ) {
		            		legalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
//		            		System.out.println(candidateDestinationCoordinate);	
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
		                        legalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
//		                        System.out.println(candidateDestinationCoordinate);
		                    }		            	    
		            		break;//if there is a piece in the direction that king can move, stop further checking in this direction.
		            	}
	            	}
	            }
		}
		return ImmutableList.copyOf(legalMoves);
    }
    
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new King(destinationCoordinate, this.getPieceAlliance());
    }
}
