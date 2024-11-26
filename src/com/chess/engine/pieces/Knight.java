package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.*;
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = {-17, -15, -10, -6, 6, 10, 15, 17};
	
	public Knight(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.KNIGHT, pieceCoordinate, pieceAlliance, true);
	}
	
	public Knight(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.KNIGHT, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
	
	@Override
    public Collection<Move> calculateMoves(final List<Tile> boardTiles, final boolean isKingInCheck) {
		final List<Move> legalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
	    // Iterate over all possible L-shaped moves for a knight
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			candidateDestinationCoordinate = this.pieceCoordinate + candidateOffset;
            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
            	final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
            	final Tile currentTile = boardTiles.get(pieceCoordinate);
            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance();
            	if (allianceOfCandidateDestinationTile != allianceOfCurrentTile) {
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
	            		if(this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
	            			if(pieceOnCandidateDestinationTile instanceof King) {
	                            legalMoves.add(new OppositeKingInCheck(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
	            			}else {
	            				legalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
	            			}
	                    }
	            	}
            	}
            }
        } 
        return ImmutableList.copyOf(legalMoves);
	}
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Knight(destinationCoordinate, this.getPieceAlliance());
    }
    
    public static int getKingCoordinate(final List<Tile> tiles, final Alliance alliance) {
	    for (Tile tile : tiles) {
	        if (tile.isTileOccupied()) {
	            Piece piece = tile.getPiece();
	            if (piece instanceof King && piece.getPieceAlliance() == alliance) {
	                return tile.getTileCoordinate();
	            }
	        }
	    }
	    throw new RuntimeException("King not found on the board");
	}
    
    private int[] getKingSideCastlingPath(int kingCoordinate, Alliance kingAlliance) {
        // Assuming standard chess board coordinates
        // Define the kingside castling paths for both white and black
        int[] kingsideCastlingPath;

        if (kingAlliance.isWhite()) {
            // White king's kingside castling path
            kingsideCastlingPath = new int[]{61, 62}; // f1, g1
        } else {
            // Black king's kingside castling path
            kingsideCastlingPath = new int[]{5, 6}; // f8, g8
        }

        // Return the kingside castling path
        return kingsideCastlingPath;
    }
    
    private int[] getQueenSideCastlingPath(int kingCoordinate, Alliance kingAlliance) {
        // Assuming standard chess board coordinates
        // Define the queenside castling paths for both white and black
        int[] queensideCastlingPath;

        if (kingAlliance.isWhite()) {
            // White king's queenside castling path
            queensideCastlingPath = new int[]{59, 58, 57}; // c1, d1, b1
        } else {
            // Black king's queenside castling path
            queensideCastlingPath = new int[]{3, 2, 1}; // c8, d8, b8
        }

        // Return the queenside castling path
        return queensideCastlingPath;
    }
}