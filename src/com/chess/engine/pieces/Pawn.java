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
    private static final int[] CANDIDATE_CAPTURE_OFFSETS = {7, 9};
    private final int advanceDirection;
    private final int initialRank;
    private final int currentRank;
    private final int promotionRank;
    private final int enPassantRank;
    
    public Pawn(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(pieceCoordinate, pieceAlliance);
        this.advanceDirection = this.pieceAlliance.getMovingDirection();  
        this.initialRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.currentRank = BoardUtils.getTileCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 7 : 2;
        this.enPassantRank = this.pieceAlliance.isWhite()? 5 : 4;
    }
     
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        addNonCapturingMoves(board, legalMoves);
        addCaptureMoves(board, legalMoves);
        return ImmutableList.copyOf(legalMoves);
    }
    
    private void addNonCapturingMoves(final Board board, final List<Move> legalMoves) {
        int coordinateOfAppliedOffset = this.pieceCoordinate + (CANDIDATE_MOVE_OFFSET * advanceDirection);
        final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
        if (!candidateDestinationTile.isTileOccupied()) {
            legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));//Add standard advance move
            if (this.currentRank == this.initialRank) {
                addDoubleAdvanceMove(board, legalMoves, coordinateOfAppliedOffset);
            }else if (this.currentRank == this.promotionRank)
                addPromotionMove(board, legalMoves, coordinateOfAppliedOffset);
        }
    }
    
    private void addDoubleAdvanceMove(final Board board, final List<Move> legalMoves, int coordinateOfAppliedOffset) {
        int coordinateOfDoubleAppliedOffset = coordinateOfAppliedOffset * 2;
        final Tile candidateDestinationTile = board.getTile(coordinateOfDoubleAppliedOffset);
        if (!candidateDestinationTile.isTileOccupied())
            legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfDoubleAppliedOffset, this));
    }
    
    private void addPromotionMove(final Board board, final List<Move> legalMoves, int coordinateOfAppliedOffset) {
        // todo Add Logic for pawn promotion
        legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
        System.out.println(coordinateOfAppliedOffset);
    }
    
    private void addCaptureMoves(final Board board, final List<Move> legalMoves) {
        for (final int candidateOffset : CANDIDATE_CAPTURE_OFFSETS) {
            int coordinateOfAppliedOffset = this.pieceCoordinate + (candidateOffset * advanceDirection);
            if (BoardUtils.isValidTileCoordinate(coordinateOfAppliedOffset)) {
            	final Tile candidateDestinationTile = board.getTile(coordinateOfAppliedOffset);
	            if(candidateDestinationTile.isTileOccupied()) { 
	            	final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
	        		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
	        		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
		            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
		            	final Tile currentTile = board.getTile(pieceCoordinate);
		            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
		            	if (allianceOfCandidateDestinationTile == allianceOfCurrentTile)
	        				legalMoves.add(new CapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this, pieceOnCandidateDestinationTile));
	        		}
	            }else if(this.currentRank == this.enPassantRank) {
	            	//check last move
	            	addEnPassantMove(board, legalMoves, coordinateOfAppliedOffset);
	            }
            }
        }
    }
    
    private void addEnPassantMove(final Board board, final List<Move> legalMoves, int coordinateOfAppliedOffset) {
        // todo Add Logic for en passant
        legalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateOfAppliedOffset, this));
        System.out.println(coordinateOfAppliedOffset);
    }
}