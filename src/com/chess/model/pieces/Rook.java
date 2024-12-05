package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.Alliance;
import com.chess.model.board.BoardUtils;
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
        List<Integer> attackPath = new ArrayList<>();// will be used in single check
		
		final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? ImmutableList.copyOf(checkingMoves) : new ArrayList<>();
		final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? ImmutableList.copyOf(oppositePlayerMoves) : new ArrayList<>();
        List<Integer> attackingPieceCoordinates = new ArrayList<>();//will be used when no check to find this.piece attacking pieces(find out if this.piece is pinned)

		if(checkingMovesToUse.size() > 1){// in double check only the king can move
			return ImmutableList.copyOf(rookPotentialLegalMoves);
		} else if(checkingMovesToUse.size() == 1){ // in single check a pawn can move to either block the check or capture the checking piece. To block the check we need the attack path of the checking piece. For capturing the checking piece I added checkingPieceCoordinate to the attackPath. 
			final Move checkingMove = checkingMovesToUse.iterator().next();
			final Piece checkingPiece = checkingMove.getPieceToMove();
			final int kingCoordinate = checkingMove.getTargetCoordinate();
			
			attackPath = CalculateMoveUtils.calculateAttackPath(
				checkingPiece,
				kingCoordinate,
				boardTiles
			);
		} else { //when no check we need to find out if this.piece is pinned. If it is pinned by only one piece then we can capture the attacking piece. If it is pinned by two or more pieces then we cannot move this piece.(see line 114)
			final int kingPosition = findKingPosition(boardTiles);
			List<Move> movesTargetingPawn = oppositePlayerMovesToUse.stream()
				.filter(move -> move.getTargetCoordinate() == this.pieceCoordinate)
				.collect(Collectors.toList());
				
			for(Move targetingMove : movesTargetingPawn) {
				Piece attackingPiece = targetingMove.getPieceToMove();
				int throughCoordinate = this.pieceCoordinate;
				
				// Keep looking through coordinates until we hit a piece or board edge
				while(true) {
					throughCoordinate = CalculateMoveUtils.getNextCoordinateInDirection(
						attackingPiece.getPieceCoordinate(), 
						throughCoordinate
					);
					
					if (!BoardUtils.isValidTileCoordinate(throughCoordinate)) {
						break;  // Stop if we hit board edge
					}
					
					Tile throughTile = boardTiles.get(throughCoordinate);
                    
					if (throughTile.isTileOccupied()) {
						Piece pieceInPath = throughTile.getPiece();
						// If it's our king, this pawn is pinned - can only capture the attacking piece
						if (pieceInPath.getPieceSymbol() == PieceSymbol.KING && 
							pieceInPath.getPieceAlliance() == this.pieceAlliance) {
							attackingPieceCoordinates.add(attackingPiece.getPieceCoordinate());
						}
						break;  // Stop when we hit any piece
					}
				}
			}
            if(attackingPieceCoordinates.size() > 1){// in double pin only the king can move
				return ImmutableList.copyOf(rookPotentialLegalMoves);
            }
		}
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
	            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
					if (checkingMovesToUse.size() == 1  && !attackPath.contains(candidateDestinationCoordinate)) {
						continue;
					}
					if(checkingMovesToUse.isEmpty() && attackingPieceCoordinates.size() == 1){
						if(candidateDestinationCoordinate != attackingPieceCoordinates.get(0)){
							continue;
						}
					}
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
