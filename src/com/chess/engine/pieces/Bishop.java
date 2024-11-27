package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Move.BlockingKingSideCastleMove;
import com.chess.engine.board.Move.BlockingQueenSideCastleMove;
import com.chess.engine.board.Move.CapturingMove;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.MoveResult;
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.google.common.collect.ImmutableList;

public class Bishop extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS= { -9, -7, 7, 9 };
	private static final int MAX_SQUARES_MOVED = 7;
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.BISHOP, pieceCoordinate, pieceAlliance, true);
	}
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.BISHOP, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
	@Override
	public String toString() {
		return this.pieceSymbol.toString();
	}
	
    @Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final int oppositeKingCoordinate, final int[] oppositeKingSideCastlePath, final int[] oppositeQueenSideCastlePath){
		final List<Move> legalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
				if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
					final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);	          
					final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
	            	final Tile currentTile = boardTiles.get(pieceCoordinate);	
	            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
	            	if (allianceOfCandidateDestinationTile == allianceOfCurrentTile) {
	            		if (!candidateDestinationTile.isTileOccupied()) {
	            		    // Check if this move blocks the castling of the opposite king
	            		    boolean blocksOpponentKingSideCastling = false;
	            		    boolean blocksOpponentQueenSideCastling = false;

	            		    for (int coordinate : oppositeKingSideCastlePath) {
	            		        if (coordinate == candidateDestinationCoordinate) {
	            		            blocksOpponentKingSideCastling = true;
	    		            		legalMoves.add(new BlockingKingSideCastleMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
	            		            break;
	            		        }
	            		    }

	            		    for (int coordinate : oppositeQueenSideCastlePath) {
	            		        if (coordinate == candidateDestinationCoordinate) {
	            		            blocksOpponentQueenSideCastling = true;
	    		            		legalMoves.add(new BlockingQueenSideCastleMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
	            		            break;
	            		        }
	            		    }
	            		    if(!blocksOpponentQueenSideCastling && !blocksOpponentKingSideCastling) {
	            		    	legalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
	            		    }
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
		                        legalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
//		                        System.out.println(candidateDestinationCoordinate);
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
    
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Bishop(destinationCoordinate, this.getPieceAlliance());
    }
}