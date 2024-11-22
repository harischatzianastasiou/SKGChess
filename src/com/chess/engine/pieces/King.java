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
import com.chess.engine.board.Move.CastleMove;
import com.chess.engine.pieces.Piece.PieceSymbol;
import com.google.common.collect.ImmutableList;

public class King extends Piece  {
	
	private final boolean isInCheck;
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -9, -8, -7, -1, 1, 7, 8, 9 };
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.KING, pieceCoordinate, pieceAlliance, true);
        this.isInCheck = false;
	}
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.KING, pieceCoordinate, pieceAlliance, isFirstMove);
        this.isInCheck = false;
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
    
    @Override
    public Collection<Move> calculateMovesConsideringOpponent(final List<Tile> boardTiles, final Collection<Move> opponentMoves, final boolean isKingInCheck, final int kingCoordinate) {
        final List<Move> legalMoves = new ArrayList<>();
        
        if (!isKingInCheck) {
            legalMoves.addAll(calculateMoves(boardTiles));
            legalMoves.addAll(calculateCastlingMoves(boardTiles, opponentMoves, isKingInCheck));
            // Filter out moves that put the King in check
            legalMoves.removeIf(move -> wouldMovePutKingInCheck( kingCoordinate, opponentMoves));
        } else {
            // Identify the pieces that threat the King
            List<Move> threatingCheckmateMoves = new ArrayList<>();
            for (Move move : opponentMoves) {
                if (move.getTargetCoordinate() == kingCoordinate) {
                	threatingCheckmateMoves.add(move);
                }
            }

            // If there's only one attacking piece, try to block or capture it
            if (threatingCheckmateMoves.size() == 1) {
                Move threatingCheckmateMove = threatingCheckmateMoves.get(0);
                int threatingPieceCoordinate = threatingCheckmateMove.getSourceCoordinate();

                // Calculate potential blocking moves
                legalMoves.addAll(calculateMoves(boardTiles));
                legalMoves.removeIf(move -> {
                    // Check if the move captures the attacking piece
                    if (move.getTargetCoordinate() == threatingPieceCoordinate) {
                        return false;
                    }
                    // Check if the move blocks the attack path
                    return !isMoveBlockingCheck(move, threatingCheckmateMove, kingCoordinate);
                });
            }else {
        	    // If there are multiple attacking pieces, the king can only move to a safe square
        	    legalMoves.addAll(calculateMoves(boardTiles));
                legalMoves.removeIf(move -> wouldMovePutKingInCheck( kingCoordinate, opponentMoves));
            }
        }

        return ImmutableList.copyOf(legalMoves);
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
   
    private Collection<Move> calculateCastlingMoves(final List<Tile> boardTiles, final Collection<Move> opponentMoves, final boolean isKingInCheck) {
        final List<Move> castlingMoves = new ArrayList<>();
        // Implement castling logic similar to the previous method, ensuring the path is not under attack
        // Check for queenside castling
        if (this.isFirstMove() && !isKingInCheck) {
            final int rookCoordinate = this.pieceCoordinate - 4;
            final Tile rookTile = boardTiles.get(rookCoordinate);
            if (rookTile.isTileOccupied() && rookTile.getPiece() instanceof Rook) {
                final Rook rook = (Rook) rookTile.getPiece();
                if (rook.isFirstMove()) {
                    final int[] betweenCoordinates = {this.pieceCoordinate - 1, this.pieceCoordinate - 2, this.pieceCoordinate - 3};
                    boolean isPathClear = true;
                    for (int coordinate : betweenCoordinates) {
                        if (boardTiles.get(coordinate).isTileOccupied() || isTileUnderAttack(coordinate, opponentMoves)) {
                            isPathClear = false;
                            break;
                        }
                    }
                    if (isPathClear) {
                        castlingMoves.add(new CastleMove(boardTiles, this.pieceCoordinate, this.pieceCoordinate - 2, this, rookCoordinate, this.pieceCoordinate - 1, rook));
                    }
                }
            }
        }

        // Check for kingside castling
        if (this.isFirstMove() && !isKingInCheck) {
            final int rookCoordinate = this.pieceCoordinate + 3;
            final Tile rookTile = boardTiles.get(rookCoordinate);
            if (rookTile.isTileOccupied() && rookTile.getPiece() instanceof Rook) {
                final Rook rook = (Rook) rookTile.getPiece();
                if (rook.isFirstMove()) {
                    final int[] betweenCoordinates = {this.pieceCoordinate + 1, this.pieceCoordinate + 2};
                    boolean isPathClear = true;
                    for (int coordinate : betweenCoordinates) {
                        if (boardTiles.get(coordinate).isTileOccupied() || isTileUnderAttack(coordinate, opponentMoves)) {
                            isPathClear = false;
                            break;
                        }
                    }
                    if (isPathClear) {
                        castlingMoves.add(new CastleMove(boardTiles, this.pieceCoordinate, this.pieceCoordinate + 2, this, rookCoordinate, this.pieceCoordinate + 1, rook));
                    }
                }
            }
        }

        return castlingMoves;
    }
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new King(destinationCoordinate, this.getPieceAlliance());
    }
}
