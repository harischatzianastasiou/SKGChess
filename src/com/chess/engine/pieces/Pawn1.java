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

public class Pawn1 extends Piece{
	
	private static final int STANDARD_PAWN_MOVED_COORDINATE = 8; 
	private static final int[] PAWN_CAPTURE_OFFSETS = { 7, 9};
	
	public Pawn1(final int pieceCoordinate, final Alliance pieceAlliance) {
		super(pieceCoordinate, pieceAlliance);
	}
	 
	@Override
	public Collection<Move> calculateLegalMoves(final Board board) {
		
		final List<Move> pawnLegalMoves = new ArrayList<>();
		int coordinateInFrontOfPawn = this.pieceCoordinate + (STANDARD_PAWN_MOVED_COORDINATE * this.getPieceAlliance().getMovingDirection());
		final Tile tileInFrontOfPawn = board.getTile(coordinateInFrontOfPawn);
		int pawnInitialRank = this.pieceAlliance.isWhite() ? 2 : 7;
		int pawnReadyForPromotionRank = this.pieceAlliance.isWhite() ? 7 : 2;
		int pawnCurrentRank = BoardUtils.getTileCoordinateRank(this.pieceCoordinate);
		
		if(!tileInFrontOfPawn.isTileOccupied()) {
			pawnLegalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateInFrontOfPawn, this));
			// If the pawn has not moved yet, it can move two squares forward.
			if(pawnCurrentRank == pawnInitialRank) {
	    		int coordinateTwoSquaresInFrontOfPawn = coordinateInFrontOfPawn * 2;
	            final Tile tileTwoSquaresInFrontOfPawn= board.getTile(coordinateTwoSquaresInFrontOfPawn);
	            
	            if(!tileTwoSquaresInFrontOfPawn.isTileOccupied()) {
	            	pawnLegalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateTwoSquaresInFrontOfPawn, this));
	            }
	        }else if(pawnCurrentRank == pawnReadyForPromotionRank) {
				// logic for pawn promotion	
				pawnLegalMoves.add(new NonCapturingMove(board, this.pieceCoordinate, coordinateInFrontOfPawn, this));
	    		System.out.println(coordinateInFrontOfPawn);
	    		// give option to promote pawn to Queen, Rook, Bishop or Knight
			}
		}
		
        // If there is an enemy piece in front of the left or right side of the pawn, it can capture it.
        for(final int candidateCaptureCoordinate : PAWN_CAPTURE_OFFSETS){
    		int pawnCaptureCoordinate = this.pieceCoordinate + ( candidateCaptureCoordinate * this.getPieceAlliance().getMovingDirection());

    		final Tile tileToBeCaptured = board.getTile(pawnCaptureCoordinate);
    		
        	if(tileToBeCaptured.isTileOccupied()) { 
        		if(this.pieceAlliance != board.getTile(pawnCaptureCoordinate).getPiece().getPieceAlliance()){
        		
        			final Alliance allianceOfTileToBeCaptured = tileToBeCaptured.getTileAlliance();
        			final Tile currentTileOfPawn = board.getTile(this.pieceCoordinate);
        			final Alliance allianceOfCurrentTileOfPawn = currentTileOfPawn.getTileAlliance(); 
		    	
        			if (allianceOfTileToBeCaptured == allianceOfCurrentTileOfPawn) {
		    		
        				final Piece targetPiece = board.getTile(pawnCaptureCoordinate).getPiece();
        				pawnLegalMoves.add(new CapturingMove(board, this.pieceCoordinate, pawnCaptureCoordinate, this, targetPiece));
        			}
		    	}
        	}
        }
        return ImmutableList.copyOf(pawnLegalMoves);
	}
}

