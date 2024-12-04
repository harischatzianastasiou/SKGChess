package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.Alliance;
import com.chess.model.board.BoardUtils;
import com.chess.model.board.CalculateMoveUtils;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.tiles.Tile;
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
	public Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles, final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {
		final List<Move> rookPotentialLegalMoves = new ArrayList<>();
		List<Integer> attackPath = new ArrayList<>();
		
		final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? checkingMoves : new ArrayList<>();
		final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? oppositePlayerMoves : new ArrayList<>();
		
		if(checkingMovesToUse.size() > 1){
			return ImmutableList.copyOf(rookPotentialLegalMoves);
		} else if(checkingMovesToUse.size() == 1){
			final Move checkingMove = checkingMovesToUse.iterator().next();
			final Piece checkingPiece = checkingMove.getPieceToMove();
			final int kingCoordinate = checkingMove.getTargetCoordinate();
			
			attackPath = CalculateMoveUtils.calculateAttackPath(
				checkingPiece,
				kingCoordinate,
				boardTiles
			);
		} else {
			List<Move> movesTargetingRook = oppositePlayerMovesToUse.stream()
				.filter(move -> move.getTargetCoordinate() == this.pieceCoordinate)
				.collect(Collectors.toList());
				
			for(Move targetingMove : movesTargetingRook) {
				Piece attackingPiece = targetingMove.getPieceToMove();
				List<Integer> potentialAttackPath = CalculateMoveUtils.calculateAttackPath(
					attackingPiece,
					CalculateMoveUtils.getNextCoordinateInDirection(
						attackingPiece.getPieceCoordinate(), 
						this.pieceCoordinate
					),
					boardTiles
				);
				attackPath.addAll(potentialAttackPath);
			}
		}
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
				if (checkingMovesToUse.size() == 1 && !attackPath.contains(candidateDestinationCoordinate)) {
					return ImmutableList.copyOf(rookPotentialLegalMoves);
				}
	            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
	            	int rankDifference = BoardUtils.getCoordinateRankDifference(candidateDestinationCoordinate, this.pieceCoordinate);
                    int fileDifference = BoardUtils.getCoordinateFileDifference(candidateDestinationCoordinate, this.pieceCoordinate);
                    if (rankDifference == 0 || fileDifference == 0) {
                        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
                        if (!candidateDestinationTile.isTileOccupied()) {
	            		    rookPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile){
		                        rookPotentialLegalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
		                    }
		            		break;//if there is a piece in the direction that bishop can move, stop further checking in this direction.
		            	}
	            	}
	            }
			}
		}
		return ImmutableList.copyOf(rookPotentialLegalMoves);
    }
        
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Rook(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
