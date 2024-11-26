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
import com.chess.engine.board.Move.CapturingPinnedPieceMove;
import com.chess.engine.board.Move.BlockingKingSideCastleMove;
import com.chess.engine.board.Move.BlockingQueenSideCastleMove;
import com.chess.engine.board.Move.OppositeKingInCheck;
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
    public Collection<Move> calculateMoves(final List<Tile> boardTiles, final boolean isKingInCheck) {
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
	            		    int opponentKingCoordinate = getKingCoordinate(boardTiles, this.pieceAlliance.getOpposite());

	            		    // Check kingside castling path
	            		    int[] kingSideCastlingPath = getKingSideCastlingPath(opponentKingCoordinate, this.pieceAlliance.getOpposite());
	            		    for (int coordinate : kingSideCastlingPath) {
	            		        if (coordinate == candidateDestinationCoordinate) {
	            		            blocksOpponentKingSideCastling = true;
	    		            		legalMoves.add(new BlockingKingSideCastleMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
	            		            break;
	            		        }
	            		    }

	            		    // Check queenside castling path
	            		    int[] queenSideCastlingPath = getQueenSideCastlingPath(opponentKingCoordinate, this.pieceAlliance.getOpposite());
	            		    for (int coordinate : queenSideCastlingPath) {
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
		            			if(pieceOnCandidateDestinationTile instanceof King) {
		                            legalMoves.add(new OppositeKingInCheck(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this));
		            			}else {	
			            		    boolean isPinned = false;
			            			// Check if the captured piece is pinned by looking further in the same direction
			                        int currentCoordinate = candidateDestinationCoordinate + candidateOffset;
			                        while (BoardUtils.isValidTileCoordinate(currentCoordinate)) {
			                            final Tile tileInDirection = boardTiles.get(currentCoordinate);
			                            if (tileInDirection.isTileOccupied()) {
			                                final Piece pieceInDirection = tileInDirection.getPiece();
			                                if (pieceInDirection.getPieceAlliance() == this.pieceAlliance) {
			                                    break; // Another piece of the same alliance blocks the path
			                                }
			                                if (pieceInDirection instanceof King && pieceInDirection.getPieceAlliance() != this.pieceAlliance) {
			                                	 // The captured piece is pinned
			                                    isPinned = true;
			    		                        legalMoves.add(new CapturingPinnedPieceMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
			                                    break;
			                                }
			                            }
			                            currentCoordinate += candidateOffset;
			                        }
			                        // Add a capturing move only if the piece is not pinned
			                        if (!isPinned) {
			                            legalMoves.add(new CapturingMove(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
			                        }	
		            			}
		            			break;// Stop further checking in this direction after capturing
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