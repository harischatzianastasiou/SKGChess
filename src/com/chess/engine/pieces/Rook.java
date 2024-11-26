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
import com.chess.engine.board.Move.CapturingPinnedPieceMove;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.Move.OppositeKingInCheck;
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.google.common.collect.ImmutableList;

public class Rook extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -8, -1, 1, 8 };
	private static final int MAX_SQUARES_MOVED = 7;
	
	public Rook(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.ROOK, pieceCoordinate, pieceAlliance, true);
	}
	
	public Rook(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.ROOK, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
	
    @Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final boolean isKingInCheck, final int oppositeKingCoordinate, final int[] oppositeKingSideCastlePath, final int[] oppositeQueenSideCastlePath){
		final List<Move> legalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
	            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
	            	int rankDifference = BoardUtils.getCoordinateRankDifference(candidateDestinationCoordinate, this.pieceCoordinate);
                    int fileDifference = BoardUtils.getCoordinateFileDifference(candidateDestinationCoordinate, this.pieceCoordinate);
                    if (rankDifference == 0 || fileDifference == 0) {
                        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
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
		                        break; // Stop further checking in this direction after capturing
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
        return new Rook(destinationCoordinate, this.getPieceAlliance());
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