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

public class Pawn extends Piece{
	
	private static final int PAWN_MOVE_COORDINATE = 8;
	private static final int[] PAWN_CAPTURE_COORDINATES = {-1,1};
	
	public Pawn(final Tile pieceTile, final Alliance pieceAlliance) {
		super(pieceTile, pieceAlliance);
	}
	
	@Override
	public Collection<Move> calculateLegalMoves(final Board board){
		
		final List<Move> legalMoves = new ArrayList<>();
		final int candidateTargetCoordinate = this.pieceTile.getTileCoordinate() +
				(this.pieceAlliance == Alliance.WHITE? PAWN_MOVE_COORDINATE : -PAWN_MOVE_COORDINATE) ;

		// Logic for first move of pawn to move 2 tiles upwards. 
		if( this.pieceAlliance == Alliance.WHITE && 
			BoardUtils.getTileCoordinateRank(this.pieceTile.getTileCoordinate()) == 1 &&
			!board.getTile(this.pieceTile.getTileCoordinate() + PAWN_MOVE_COORDINATE).isTileOccupied() && 
			!board.getTile(this.pieceTile.getTileCoordinate() + PAWN_MOVE_COORDINATE*2).isTileOccupied()){
        	
			final Tile candidateTargetTile = board.getTile(candidateTargetCoordinate+8);
    		legalMoves.add(new NonCapturingMove(board, this.pieceTile, candidateTargetTile, this));
    		System.out.println(candidateTargetCoordinate);
    		
		} else if( this.pieceAlliance == Alliance.BLACK &&
				   BoardUtils.getTileCoordinateRank(this.pieceTile.getTileCoordinate()) == 8 && 
				   !board.getTile(this.pieceTile.getTileCoordinate() + PAWN_MOVE_COORDINATE).isTileOccupied() &&
				   !board.getTile(this.pieceTile.getTileCoordinate() + PAWN_MOVE_COORDINATE*2).isTileOccupied()){
        	
			final Tile candidateTargetTile = board.getTile(candidateTargetCoordinate-8);
    		legalMoves.add(new NonCapturingMove(board, this.pieceTile, candidateTargetTile, this));
    		System.out.println(candidateTargetCoordinate);
		}
		
		// logic for pawn capturing tiles to the left and right. 
		for(final int candidateCaptureCoordinate : PAWN_CAPTURE_COORDINATES){
			final Tile candidateTargetTile = board.getTile(candidateTargetCoordinate + candidateCaptureCoordinate);
			if(candidateTargetTile.isTileOccupied() && 
			   candidateTargetTile.getPiece().getPieceAlliance()!= this.getPieceAlliance()) {
				
				final Alliance candidateTargetTileAlliance = candidateTargetTile.getTileAlliance();
		    	final Alliance sourceTileAlliance = this.pieceTile.getTileAlliance(); 
		    	
		    	if (candidateTargetTileAlliance != sourceTileAlliance) {
		    		 legalMoves.add(new CapturingMove(board, this.pieceTile, candidateTargetTile, this, candidateTargetTile.getPiece()));
		    		 System.out.println(candidateTargetCoordinate);
		    	}
			}
		}
		
		final Tile candidateTargetTile = board.getTile(candidateTargetCoordinate);
		if(!candidateTargetTile.isTileOccupied() ) {
			// logic for pawn promotion
			if( this.pieceAlliance == Alliance.WHITE && 
				BoardUtils.getTileCoordinateRank(this.pieceTile.getTileCoordinate()) == 7 && 
				!board.getTile(this.pieceTile.getTileCoordinate() + PAWN_MOVE_COORDINATE).isTileOccupied()){
	    		
				legalMoves.add(new NonCapturingMove(board, this.pieceTile, candidateTargetTile, this));
	    		System.out.println(candidateTargetCoordinate);
	    		// give option to promote pawn to Queen, Rook, Bishop or Knight.
			} else if( this.pieceAlliance == Alliance.BLACK && 
					   BoardUtils.getTileCoordinateRank(this.pieceTile.getTileCoordinate()) == 2 && 
					   !board.getTile(this.pieceTile.getTileCoordinate() + PAWN_MOVE_COORDINATE).isTileOccupied()){
	    		
				legalMoves.add(new NonCapturingMove(board, this.pieceTile, candidateTargetTile, this));
	    		System.out.println(candidateTargetCoordinate);
	    		//give option to promote pawn to Queen, Rook, Bishop or Knight.
			} else {
				legalMoves.add(new NonCapturingMove(board, this.pieceTile, candidateTargetTile, this));
    			System.out.println(candidateTargetCoordinate);
		    }
		}
		return ImmutableList.copyOf(legalMoves);
		// missing en passant
		// black and white moving -8 or +8 must be refactored.
	}
}
