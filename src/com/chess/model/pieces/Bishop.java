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

public class Bishop extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS= { -9, -7, 7, 9 };
	private static final int MAX_SQUARES_MOVED = 7;
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.BISHOP, pieceCoordinate, pieceAlliance, true);
	}
	
	public Bishop(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.BISHOP, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
	@Override
	public String toString() {
		return this.pieceSymbol.toString();
	}
	
    @Override
	public Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles, final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {
        final List<Move> bishopPotentialLegalMoves = new ArrayList<>();
        List<Integer> attackPath = new ArrayList<>();// will be used in single check
		List<Integer> attackingPieceCoordinates = new ArrayList<>();//will be used when no check to find this.piece attacking pieces(find out if this.piece is pinned)

		System.out.println(checkingMoves);
		if(checkingMoves != null){
			if(checkingMoves.size() > 1){// in double check only the king can move
				System.out.println("paok1");
				return ImmutableList.copyOf(bishopPotentialLegalMoves);
			} else if(checkingMoves.size() == 1){ // in single check a pawn can move to either block the check or capture the checking piece. To block the check we need the attack path of the checking piece. For capturing the checking piece I added checkingPieceCoordinate to the attackPath. 
				System.out.println("paok2");
				final Move checkingMove = checkingMoves.iterator().next();
				final Piece checkingPiece = checkingMove.getPieceToMove();
				final int kingCoordinate = checkingMove.getTargetCoordinate();
				
				attackPath = CalculateMoveUtils.calculateAttackPath(
					checkingPiece,
					kingCoordinate,
					boardTiles
				);
			} else if(checkingMoves.size() == 0) { //when no check we need to find out if this.piece is pinned. If it is pinned by only one piece then we can capture the attacking piece. If it is pinned by two or more pieces then we cannot move this piece.(see line 114)
				System.out.println("paok3");
				final int kingPosition = findKingPosition(boardTiles);
				List<Move> movesTargetingPawn = oppositePlayerMoves.stream()
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
					return ImmutableList.copyOf(bishopPotentialLegalMoves);
				}
			}
		}
		final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? ImmutableList.copyOf(checkingMoves) : new ArrayList<>();
		final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? ImmutableList.copyOf(oppositePlayerMoves) : new ArrayList<>();
        
		int candidateDestinationCoordinate;
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
				int total_offset = candidateOffset * squaresMoved;
				candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
				if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
					System.out.println("this.pieceCoordinate: " + this.pieceCoordinate);
					System.out.println("candidateDestinationCoordinate: " + candidateDestinationCoordinate);
				System.out.println("attackPath: " + attackPath);
				System.out.println("checkingMovesToUse.size(): " + checkingMovesToUse.size());
				System.out.println("attackPath.contains(candidateDestinationCoordinate): " + attackPath.contains(candidateDestinationCoordinate));
				System.out.println("attackingPieceCoordinates: " + attackingPieceCoordinates);
					if (checkingMovesToUse.size() == 1) {  // If in check
						if (!attackPath.contains(candidateDestinationCoordinate)) {  // And move doesn't block check
							continue;  // Skip this move but keep checking others
						}
					}
					if(checkingMovesToUse.isEmpty() && attackingPieceCoordinates.size() == 1){
						if(candidateDestinationCoordinate != attackingPieceCoordinates.get(0)){
							continue;  // Skip this move but keep checking others
						}
					}
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
        return new Bishop(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
