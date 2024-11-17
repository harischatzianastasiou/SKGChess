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
import com.chess.engine.pieces.Piece.PieceSymbol;

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
        this.currentRank = BoardUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 7 : 2;
        this.enPassantRank = this.pieceAlliance.isWhite()? 5 : 4;
    }
    
	@Override
	public String toString() {
		return PieceSymbol.PAWN.toString();
	}
     
    @Override
    public Collection<Move> calculateMoves(final List<Tile> boardTiles) {
        final List<Move> legalMoves = new ArrayList<>();
        addNonCapturingMoves(boardTiles,legalMoves);
        addCaptureMoves(boardTiles,legalMoves);
        return ImmutableList.copyOf(legalMoves);
    }
    
    private void addNonCapturingMoves(final List<Tile> boardTiles, final List<Move> legalMoves) {
        int candidateDestinationCoordinate = this.pieceCoordinate + (CANDIDATE_MOVE_OFFSET * advanceDirection);
        if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
	        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
	        if (!candidateDestinationTile.isTileOccupied()) {
	            legalMoves.add(new NonCapturingMove(this.pieceCoordinate, candidateDestinationCoordinate, this));//Add standard advance move
	            if (this.currentRank == this.initialRank) {
	                addDoubleAdvanceMove(boardTiles,legalMoves, candidateDestinationCoordinate);
	            }else if (this.currentRank == this.promotionRank)
	                addPromotionMove(boardTiles,legalMoves, candidateDestinationCoordinate);
	        }
        }
    }
    
    private void addDoubleAdvanceMove(final List<Tile> boardTiles, final List<Move> legalMoves, int candidateDestinationCoordinate) {
        int CandidateDoubleDestinationCoordinate = candidateDestinationCoordinate * 2;
        final Tile candidateDestinationTile = boardTiles.get(CandidateDoubleDestinationCoordinate);
        if (!candidateDestinationTile.isTileOccupied())
            legalMoves.add(new NonCapturingMove(this.pieceCoordinate, CandidateDoubleDestinationCoordinate, this));
    }
    
    private void addPromotionMove(final List<Tile> boardTiles, final List<Move> legalMoves, int candidateDestinationCoordinate) {
        // todo Add Logic for pawn promotion
        legalMoves.add(new NonCapturingMove(this.pieceCoordinate, candidateDestinationCoordinate, this));
        System.out.println(candidateDestinationCoordinate);
    }
    
    private void addCaptureMoves(final List<Tile> boardTiles, final List<Move> legalMoves) {
        for (final int candidateOffset : CANDIDATE_CAPTURE_OFFSETS) {
            int candidateDestinationCoordinate = this.pieceCoordinate + (candidateOffset * advanceDirection);
            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
            	final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
	            if(candidateDestinationTile.isTileOccupied()) { 
	            	final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
	        		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
	        		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
		            	final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
		            	final Tile currentTile = boardTiles.get(pieceCoordinate);
		            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
		            	if (allianceOfCandidateDestinationTile == allianceOfCurrentTile)
	        				legalMoves.add(new CapturingMove(this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
	        		}
	            }else if(this.currentRank == this.enPassantRank) {
	            	//check last move
	            	addEnPassantMove(legalMoves, candidateDestinationCoordinate);
	            }
            }
        }
    }
    
    private void addEnPassantMove(final List<Move> legalMoves, int coordinateOfAppliedOffset) {
        // todo Add Logic for en passant
        legalMoves.add(new NonCapturingMove(this.pieceCoordinate, coordinateOfAppliedOffset, this));
        System.out.println(coordinateOfAppliedOffset);
    }
}