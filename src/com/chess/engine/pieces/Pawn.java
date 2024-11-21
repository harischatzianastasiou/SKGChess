package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.GameHistory;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;
import com.chess.engine.board.Move.CapturingMove;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.Move.PawnJumpMove;
import com.chess.engine.board.Move.PawnEnPassantAttack;
import com.chess.engine.board.Move.PawnPromotionMove;
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
        super(PieceSymbol.PAWN,pieceCoordinate, pieceAlliance, true);
        this.advanceDirection = this.pieceAlliance.getMovingDirection();  
        this.initialRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.currentRank = BoardUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 7 : 2;
        this.enPassantRank = this.pieceAlliance.isWhite()? 5 : 4;
    }
    
    public Pawn(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.PAWN,pieceCoordinate, pieceAlliance, isFirstMove);
        this.advanceDirection = this.pieceAlliance.getMovingDirection();  
        this.initialRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.currentRank = BoardUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 7 : 2;
        this.enPassantRank = this.pieceAlliance.isWhite()? 5 : 4;
    }
    
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
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
	            legalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));//Add standard advance move
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
            legalMoves.add(new PawnJumpMove(boardTiles,this.pieceCoordinate, CandidateDoubleDestinationCoordinate, this));
    }
    
    private void addPromotionMove(final List<Tile> boardTiles, final List<Move> legalMoves, int candidateDestinationCoordinate) {
    	if(GameHistory.getInstance().getLastMove() instanceof PawnPromotionMove) {
    		legalMoves.add(new PawnPromotionMove(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this));
    	}
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
	        				legalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
	        		}
	            }else if(this.currentRank == this.enPassantRank) {
	            	//check last move
	            	addEnPassantMove(boardTiles,legalMoves, candidateDestinationCoordinate);
	            }
            }
        }
    }
    
    private void addEnPassantMove(final List<Tile> boardTiles,final List<Move> legalMoves, int coordinateOfAppliedOffset) {
    	if(GameHistory.getInstance().getLastMove() instanceof PawnJumpMove) {
    		final int EnPassantDestinationCoordinate = coordinateOfAppliedOffset + (CANDIDATE_MOVE_OFFSET * pieceAlliance.getMovingDirection());
    		legalMoves.add(new PawnEnPassantAttack(boardTiles,this.pieceCoordinate, EnPassantDestinationCoordinate, this, GameHistory.getInstance().getLastMove().getPieceToMove()));
    	}
    }
    
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Pawn(destinationCoordinate, this.getPieceAlliance());
    }   
}