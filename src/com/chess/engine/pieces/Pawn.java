package com.chess.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.GameHistory;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.CapturingMove;
import com.chess.engine.board.Move.NonCapturingMove;
import com.chess.engine.board.Move.PawnEnPassantAttack;
import com.chess.engine.board.Move.PawnJumpMove;
import com.chess.engine.board.Move.PawnPromotionMove;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

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
        this.initialRank = this.pieceAlliance.isWhite() ? 7: 2;
        this.currentRank = BoardUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.enPassantRank = this.pieceAlliance.isWhite()? 4 : 5;
    }
    
    public Pawn(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.PAWN,pieceCoordinate, pieceAlliance, isFirstMove);
        this.advanceDirection = this.pieceAlliance.getMovingDirection();  
        this.initialRank = this.pieceAlliance.isWhite() ? 7 : 2;
        this.currentRank = BoardUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.enPassantRank = this.pieceAlliance.isWhite()? 4 : 5;
    }
    
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
     
    @Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final Alliance currentPlayer) {
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
                if(currentRank != this.promotionRank){
	                legalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));//Add standard advance move
	                if (currentRank == this.initialRank) {
	                    addDoubleAdvanceMove(boardTiles,legalMoves);
	                }
	            }else
	                addPromotionMove(boardTiles,legalMoves, candidateDestinationCoordinate);
	        }
        }
    }
    
    private void addDoubleAdvanceMove(final List<Tile> boardTiles, final List<Move> legalMoves) {
        int CandidateDoubleDestinationCoordinate = this.pieceCoordinate + (2 * CANDIDATE_MOVE_OFFSET * advanceDirection);
        final Tile candidateDestinationTile = boardTiles.get(CandidateDoubleDestinationCoordinate);
        if (!candidateDestinationTile.isTileOccupied())
            legalMoves.add(new PawnJumpMove(boardTiles,this.pieceCoordinate, CandidateDoubleDestinationCoordinate, this));
    }
    
    private void addPromotionMove(final List<Tile> boardTiles, final List<Move> legalMoves, int candidateDestinationCoordinate) {
    	legalMoves.add(new PawnPromotionMove(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this));
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
	            }else if(currentRank == this.enPassantRank) {
	            	//check last move
	            	addEnPassantMove(boardTiles,legalMoves, candidateDestinationCoordinate);
	            }
            }
        }
    }
    
    private void addEnPassantMove(final List<Tile> boardTiles, final List<Move> legalMoves, int candidateDestinationCoordinate) {
        Move lastMove = GameHistory.getInstance().getLastMove();
        if (!(lastMove instanceof PawnJumpMove) || lastMove.getPieceToMove().getPieceAlliance() == this.pieceAlliance || BoardUtils.getCoordinateFile(lastMove.getTargetCoordinate()) != BoardUtils.getCoordinateFile(candidateDestinationCoordinate)) {
            return;
        }
        Piece pieceToCapture = boardTiles.get(lastMove.getTargetCoordinate()).getPiece();
        // Add the en passant move
        legalMoves.add(new PawnEnPassantAttack(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this, pieceToCapture));
    }
    
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Pawn(destinationCoordinate, this.getPieceAlliance(),false);
    } 
    
    public Piece promotePawn(int destinationCoordinate, String newPieceType) {
        switch (newPieceType.toUpperCase()) {
            case "QUEEN":
                return new Queen(destinationCoordinate, this.getPieceAlliance(), false);
            case "ROOK":
                return new Rook(destinationCoordinate, this.getPieceAlliance(), false);
            case "BISHOP":
                return new Bishop(destinationCoordinate, this.getPieceAlliance(), false);
            case "KNIGHT":
                return new Knight(destinationCoordinate, this.getPieceAlliance(), false);
            default:
                throw new IllegalArgumentException("Invalid piece type for promotion: " + newPieceType);
        }
    }
    // add case for pawn blocking castle of king
}
