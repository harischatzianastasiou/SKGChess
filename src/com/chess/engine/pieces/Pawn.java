package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;
import com.chess.engine.board.Move.CapturingMove;
import com.chess.engine.board.Move.NonCapturingMove;

public class Pawn extends Piece {
    
    private static final int CANDIDATE_MOVE_OFFSET = 8; 
    private static final int[] CANDIDATE_CAPTURE_OFFSETS = { 7, 9 };
    private int pawnAdvanceDirection = this.getPieceAlliance().getMovingDirection();

    
    public Pawn(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
    }
     
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        
        final List<Move> legalMoves = new ArrayList<>();
        addStandardMoves(board, legalMoves);
        addCaptureMoves(board, legalMoves);
        
        return ImmutableList.copyOf(legalMoves);
    }
    
    private void addStandardMoves(final Board board, final List<Move> legalMoves) {
    	 
        int coordinateOfAppliedOffset = this.pieceCoordinate + (CANDIDATE_MOVE_OFFSET * pawnAdvanceDirection);
        final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
        int pawnInitialRank = this.pieceAlliance.isWhite() ? 2 : 7;
        int pawnReadyForPromotionRank = this.pieceAlliance.isWhite() ? 7 : 2;
        int pawnCurrentRank = BoardUtils.getTileCoordinateRank(this.pieceCoordinate);
        
        if (!candidateDestinationTile.isTileOccupied()) {
            legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
            
            if (pawnCurrentRank == pawnInitialRank) {
                addDoubleMove(board, legalMoves, coordinateOfAppliedOffset);
            } else if (pawnCurrentRank == pawnReadyForPromotionRank) {
                addPromotionMove(board, legalMoves, coordinateOfAppliedOffset);
            }
        }
    }
    
    private void addDoubleMove(final Board board, final List<Move> legalMoves, int coordinateOfFrontSquare) {
        int coordinateOfAppliedOffset = coordinateOfFrontSquare * 2;
        final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
        
        if (!candidateDestinationTile.isTileOccupied()) {
            legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
        }
    }
    
    private void addPromotionMove(final Board board, final List<Move> legalMoves, int coordinateOfCandidateMove) {
        // Logic for pawn promotion
        legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfCandidateMove, this));
        System.out.println(coordinateOfCandidateMove);
        // Add logic to promote pawn to Queen, Rook, Bishop, or Knight
    }
    
    private void addCaptureMoves(final Board board, final List<Move> legalMoves) {
        for (final int candidateOffset : CANDIDATE_CAPTURE_OFFSETS) {
            int coordinateOfAppliedOffset = this.pieceCoordinate + (candidateOffset * pawnAdvanceDirection);
            
            if (BoardUtils.isValidTileCoordinate(coordinateOfAppliedOffset)) {
            	final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
            
	            if(candidateDestinationTile.isTileOccupied()) { 
	            	final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
	        		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
	        		
	        		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
	        		
		            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
		            	final Tile currentTile = board.getTile(pieceCoordinate);
		            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
		            	
		            	if (allianceOfCandidateDestinationTile == allianceOfCurrentTile) {	
	        				final Piece pieceCaptured = board.getTile(coordinateOfAppliedOffset).getPiece();
	        				legalMoves.add(new CapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this, pieceOnCandidateDestinationTile));
	        			}
	        		}
	            }
            }
        }
    }
}