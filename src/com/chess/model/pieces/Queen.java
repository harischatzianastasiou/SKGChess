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

public class Queen extends Piece {
    
    private static final int[] ROOK_MOVE_OFFSETS = { -8, -1, 1, 8 };
    private static final int[] BISHOP_MOVE_OFFSETS = { -9, -7, 7, 9 };
    private static final int MAX_SQUARES_MOVED = 7;

    public Queen(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.QUEEN, pieceCoordinate, pieceAlliance, true);
    }

    public Queen(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.QUEEN, pieceCoordinate, pieceAlliance, isFirstMove);
    }

    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }

    @Override
	public Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles, final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {
        final List<Move> queenPotentialLegalMoves = new ArrayList<>();
		List<Integer> attackPath = new ArrayList<>();
		
		final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? checkingMoves : new ArrayList<>();
		final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? oppositePlayerMoves : new ArrayList<>();
		
		if(checkingMovesToUse.size() > 1){
			return ImmutableList.copyOf(queenPotentialLegalMoves);
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
			List<Move> movesTargetingQueen = oppositePlayerMovesToUse.stream()
				.filter(move -> move.getTargetCoordinate() == this.pieceCoordinate)
				.collect(Collectors.toList());
				
			for(Move targetingMove : movesTargetingQueen) {
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
        queenPotentialLegalMoves.addAll(calculatePotentialLegalMovesForRook(boardTiles, checkingMoves, attackPath));
        queenPotentialLegalMoves.addAll(calculatePotentialLegalMovesForBishop(boardTiles, checkingMoves, attackPath));
        return ImmutableList.copyOf(queenPotentialLegalMoves);
    }

    public Collection<Move> calculatePotentialLegalMovesForRook(final List<Tile> boardTiles, final Collection<Move> checkingMoves, final List<Integer> attackPath) {
		final List<Move> rookPotentialLegalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : ROOK_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
				if (checkingMoves.size() == 1 && !attackPath.contains(candidateDestinationCoordinate)) {
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

    public Collection<Move> calculatePotentialLegalMovesForBishop(final List<Tile> boardTiles, final Collection<Move> checkingMoves, final List<Integer> attackPath) {
		final List<Move> bishopPotentialLegalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
		for (final int candidateOffset : BISHOP_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
				if (checkingMoves.size() == 1 && !attackPath.contains(candidateDestinationCoordinate)) {
					return ImmutableList.copyOf(bishopPotentialLegalMoves);
				}
				if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
					final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);	          
					final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
	            	final Tile currentTile = boardTiles.get(pieceCoordinate);	
	            	final Alliance allianceOfCurrentTile = currentTile.getTileAlliance(); 
	            	if (allianceOfCandidateDestinationTile == allianceOfCurrentTile) {
	            		if (!candidateDestinationTile.isTileOccupied()) { 
							bishopPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
		            	}else {
		            		final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
		            		final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
		            		if( this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile ){
		                        bishopPotentialLegalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
		                    }
		            		break;//if there is a piece in the direction that bishop can move, stop further checking in this direction.
		            	}
	            	}
	            	else break; //if the current vector does not apply for the pieceTile(eg for tiles that are on 1st or 8th rank), stop further checking in this direction.
	            }
	            else break;//If the candidateTargetCoordinate is out of boundaries, stop further checking in this direction.
			} 
		}
		return ImmutableList.copyOf(bishopPotentialLegalMoves);
    }

    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Queen(destinationCoordinate, this.getPieceAlliance(), false);
    }
}
