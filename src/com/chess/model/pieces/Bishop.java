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
import com.chess.model.player.CurrentPlayer;
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
	// Opponent player's moves don't need to be validated for check/pin
	public Collection<Move> calculateOpponentMoves(List<Tile> boardTiles) {
		final List<Move> bishopPotentialLegalMoves = new ArrayList<>();
		int candidateDestinationCoordinate;
        for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
            for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
                int total_offset = candidateOffset * squaresMoved;
                candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
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
	// Current player's moves need to be validated for check/pin
	public Collection<Move> calculateCurrentPlayerMoves(
		final List<Tile> boardTiles,
		final Collection<Move> checkingMoves, 
		final Collection<Move> oppositePlayerMoves) {
        
		final List<Move> bishopPotentialLegalMoves = new ArrayList<>();
        List<Integer> checkingPieceAttackPath = new ArrayList<>();// will be used in single check
		List<Integer> pinningPiecesOfPawnCoordinates = new ArrayList<>();//will be used when no check to find the pieces that may pin this.piece

        // Ensure that we have non-null collections.
        final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? ImmutableList.copyOf(checkingMoves) : new ArrayList<>();
        final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? ImmutableList.copyOf(oppositePlayerMoves) : new ArrayList<>();

        if(checkingMovesToUse.size() > 1){ //in double check bishop cannot block both checks so he cannot move. Only the king can move.
            return ImmutableList.copyOf(bishopPotentialLegalMoves);
        } else if(checkingMovesToUse.size() == 1){ // in single check bishop can block the check by moving to the attack path of the checking piece
            final Move checkingMove = checkingMovesToUse.iterator().next();
            final Piece checkingPiece = checkingMove.getPieceToMove();
            final int kingCoordinate = checkingMove.getTargetCoordinate();
            checkingPieceAttackPath.addAll(CalculateMoveUtils.calculateAttackPath(checkingPiece, kingCoordinate, boardTiles));
        } else { // in no check bishop can move as long as it is not pinned. Here we check if it is pinned.
            final int kingPosition = CurrentPlayer.getKingCoordinate(boardTiles, this.pieceAlliance);
            List<Move> movesTargetingPawn = oppositePlayerMovesToUse.stream()
                .filter(move -> move.getTargetCoordinate() == this.pieceCoordinate)
                .collect(Collectors.toList());
                
            for(Move targetingMove : movesTargetingPawn) {
				if(targetingMove.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN 
				|| targetingMove.getPieceToMove().getPieceSymbol() == PieceSymbol.KING
				|| targetingMove.getPieceToMove().getPieceSymbol() == PieceSymbol.KNIGHT){
					continue; // Pawns, Kings and Knights cannot pin other pieces
				}
					Piece pinningPieceOfPawn= targetingMove.getPieceToMove();
					int throughCoordinate = this.pieceCoordinate;
					// Keep looking through coordinates until we hit a piece or board edge
					while(true) {
						//must check alliance of throughTile and currentPieceTile
                    throughCoordinate = CalculateMoveUtils.getNextCoordinateInDirection(
                        pinningPieceOfPawn.getPieceCoordinate(), 
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
							 pinningPiecesOfPawnCoordinates.add(pinningPieceOfPawn.getPieceCoordinate());
                        }
                        break;  // Stop when we hit any piece
                    }
                }
            }
            if(pinningPiecesOfPawnCoordinates.size() > 1){// in double pin only the king can move
                return ImmutableList.copyOf(bishopPotentialLegalMoves);
            }
        }

        int candidateDestinationCoordinate;
        for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
            for(int squaresMoved=1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++ ) {
                int total_offset = candidateOffset * squaresMoved;
                candidateDestinationCoordinate = this.pieceCoordinate + total_offset;
                if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
					final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
                    if (checkingMovesToUse.size() == 1) {  // If in check
                        if (!checkingPieceAttackPath.contains(candidateDestinationCoordinate)) {  // And move doesn't block check
							if(candidateDestinationTile.isTileOccupied()){
								break;
							}else{
								continue;  // Skip this move but keep checking others
							}
						}
                    }
                    if(checkingMovesToUse.isEmpty() && pinningPiecesOfPawnCoordinates.size() == 1){
                        if(candidateDestinationCoordinate != pinningPiecesOfPawnCoordinates.get(0)){
                            if(candidateDestinationTile.isTileOccupied()){
								break;
							}else{
								continue;  // Skip this move but keep checking others
							}
                        }
                    }
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
