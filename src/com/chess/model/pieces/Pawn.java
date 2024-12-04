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
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.tiles.Tile;
import com.chess.util.GameHistory;
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
	public Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles, final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {
        final List<Move> pawnPotentialLegalMoves = new ArrayList<>();
        List<Integer> attackPath = new ArrayList<>();
		
		final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? checkingMoves : new ArrayList<>();
		final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? oppositePlayerMoves : new ArrayList<>();
		
		if(checkingMovesToUse.size() > 1){
			return ImmutableList.copyOf(pawnPotentialLegalMoves);
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
			List<Move> movesTargetingPawn = oppositePlayerMovesToUse.stream()
				.filter(move -> move.getTargetCoordinate() == this.pieceCoordinate)
				.collect(Collectors.toList());
				
			for(Move targetingMove : movesTargetingPawn) {
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
    	addPotentialNonCapturingMoves(boardTiles,pawnPotentialLegalMoves, checkingMovesToUse, attackPath);
    	addPotenialCaptureMoves(boardTiles,pawnPotentialLegalMoves, checkingMovesToUse, attackPath);
        return ImmutableList.copyOf(pawnPotentialLegalMoves);
    }
    
    private void addPotentialNonCapturingMoves(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, final Collection<Move> checkingMoves, final List<Integer> attackPath) {
    	int candidateDestinationCoordinate = this.pieceCoordinate + (CANDIDATE_MOVE_OFFSET * advanceDirection);
        if (checkingMoves.size() == 1 && !attackPath.contains(candidateDestinationCoordinate)) {
            return;
        }
        if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
	        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
	        if (!candidateDestinationTile.isTileOccupied()) {
                if(currentRank != this.promotionRank){
	                pawnPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));//Add standard advance move
	                if (currentRank == this.initialRank) {
	                    addPotenialDoubleAdvanceMove(boardTiles,pawnPotentialLegalMoves, checkingMoves, attackPath);
	                }
	            }else
	                addPotentialPromotionMove(boardTiles,pawnPotentialLegalMoves, candidateDestinationCoordinate);
	        }
        }
    }
    
    private void addPotenialDoubleAdvanceMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, final Collection<Move> checkingMoves, final List<Integer> attackPath) {
        int CandidateDoubleDestinationCoordinate = this.pieceCoordinate + (2 * CANDIDATE_MOVE_OFFSET * advanceDirection);
        if (checkingMoves.size() == 1 && !attackPath.contains(CandidateDoubleDestinationCoordinate)) {
            return;
        }
        final Tile candidateDestinationTile = boardTiles.get(CandidateDoubleDestinationCoordinate);
        if (!candidateDestinationTile.isTileOccupied())
            pawnPotentialLegalMoves.add(new PawnJumpMove(boardTiles,this.pieceCoordinate, CandidateDoubleDestinationCoordinate, this));
    }
    
    private void addPotentialPromotionMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, int candidateDestinationCoordinate) {
    	pawnPotentialLegalMoves.add(new PawnPromotionMove(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this));
    }
    
    private void addPotenialCaptureMoves(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, final Collection<Move> checkingMoves, final List<Integer> attackPath) {
    	for (final int candidateOffset : CANDIDATE_CAPTURE_OFFSETS) {
            int candidateDestinationCoordinate = this.pieceCoordinate + (candidateOffset * advanceDirection);
            if (checkingMoves.size() == 1 && !attackPath.contains(candidateDestinationCoordinate)) {
                return;
            }
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
                        if(currentRank != this.promotionRank)
                        pawnPotentialLegalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
                        else
                            addPotentialPromotionMove(boardTiles, pawnPotentialLegalMoves, candidateDestinationCoordinate);
                    }
	            }else if(currentRank == this.enPassantRank) {
	            	//check last move
	            	addPotentialEnPassantMove(boardTiles, pawnPotentialLegalMoves, candidateDestinationCoordinate);
	            }
            }
        }
    }
    
    private void addPotentialEnPassantMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, int candidateDestinationCoordinate) {
        Move lastMove = GameHistory.getInstance().getLastMove();
        if (!(lastMove instanceof PawnJumpMove) || lastMove.getPieceToMove().getPieceAlliance() == this.pieceAlliance || BoardUtils.getCoordinateFile(lastMove.getTargetCoordinate()) != BoardUtils.getCoordinateFile(candidateDestinationCoordinate)) {
            return;
        }
        Piece pieceToCapture = boardTiles.get(lastMove.getTargetCoordinate()).getPiece();
        // Add the en passant move
        pawnPotentialLegalMoves.add(new PawnEnPassantAttack(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this, pieceToCapture));
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
