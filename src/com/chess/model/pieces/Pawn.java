package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.Alliance;
import com.chess.model.board.BoardUtils;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.player.CurrentPlayer;
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
	// Opponent player's moves don't need to be validated for check/pin
	public Collection<Move> calculateOpponentMoves(List<Tile> boardTiles) {
        final List<Move> pawnPotentialLegalMoves = new ArrayList<>();
        addPotentialNonCapturingMoves(boardTiles, pawnPotentialLegalMoves);
        addPotenialCaptureMoves(boardTiles, pawnPotentialLegalMoves);

        return ImmutableList.copyOf(pawnPotentialLegalMoves);
    }

    private void addPotentialNonCapturingMoves(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves) {
        int candidateDestinationCoordinate = this.pieceCoordinate + (CANDIDATE_MOVE_OFFSET * advanceDirection);
        if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
	        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
	        if (!candidateDestinationTile.isTileOccupied()) {
                if(currentRank != this.promotionRank){
	                pawnPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));//Add standard advance move
	                if (currentRank == this.initialRank) {
	                    addPotenialDoubleAdvanceMove(boardTiles,pawnPotentialLegalMoves);
	                }
	            }else
	                addPotentialPromotionMove(boardTiles,pawnPotentialLegalMoves, candidateDestinationCoordinate);
	        }
        }
    }
    
    private void addPotenialDoubleAdvanceMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves) {
        int CandidateDoubleDestinationCoordinate = this.pieceCoordinate + (2 * CANDIDATE_MOVE_OFFSET * advanceDirection);
        final Tile candidateDestinationTile = boardTiles.get(CandidateDoubleDestinationCoordinate);
        if (!candidateDestinationTile.isTileOccupied())
            pawnPotentialLegalMoves.add(new PawnJumpMove(boardTiles,this.pieceCoordinate, CandidateDoubleDestinationCoordinate, this));
    }
    
    private void addPotentialPromotionMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, int candidateDestinationCoordinate) {
        pawnPotentialLegalMoves.add(new PawnPromotionMove(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this));
    }
    
    private void addPotenialCaptureMoves(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves) {
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
	// Current player's moves need to be validated for check/pin
	public Collection<Move> calculateCurrentPlayerMoves(
		final List<Tile> boardTiles,
		final Collection<Move> checkingMoves, 
		final Collection<Move> oppositePlayerMoves) {
        final List<Move> pawnPotentialLegalMoves = new ArrayList<>();
        List<Integer> checkingPieceAttackPath = new ArrayList<>();// will be used in single check
		List<Integer> pinningPiecesOfPawnCoordinates = new ArrayList<>();//will be used when no check to find the pieces that may pin this.piece

        // Ensure that we have non-null collections.
        final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? ImmutableList.copyOf(checkingMoves) : new ArrayList<>();
        final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? ImmutableList.copyOf(oppositePlayerMoves) : new ArrayList<>();

        if(checkingMovesToUse.size() > 1){ //in double check bishop cannot block both checks so he cannot move. Only the king can move.
            return ImmutableList.copyOf(pawnPotentialLegalMoves);
        } else if(checkingMovesToUse.size() == 1){ // in single check bishop can block the check by moving to the attack path of the checking piece
            final Move checkingMove = checkingMovesToUse.iterator().next();
            final Piece checkingPiece = checkingMove.getPieceToMove();
            final int kingCoordinate = checkingMove.getTargetCoordinate();
            checkingPieceAttackPath.addAll(CalculateMoveUtils.calculateAttackPath(checkingPiece, kingCoordinate, boardTiles));
        } else { // in no check pawn can move as long as it is not pinned. Here we check if it is pinned.
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
                return ImmutableList.copyOf(pawnPotentialLegalMoves);
            }
        }
        addPotentialNonCapturingMoves(boardTiles, pawnPotentialLegalMoves, checkingMovesToUse, checkingPieceAttackPath, pinningPiecesOfPawnCoordinates);
        addPotenialCaptureMoves(boardTiles, pawnPotentialLegalMoves, checkingMovesToUse, checkingPieceAttackPath, pinningPiecesOfPawnCoordinates);

        return ImmutableList.copyOf(pawnPotentialLegalMoves);
    }
    
    private void addPotentialNonCapturingMoves(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, final Collection<Move> checkingMovesToUse, final List<Integer> checkingPieceAttackPath, final List<Integer> pinningPiecesOfPawnCoordinates) {
        int candidateDestinationCoordinate = this.pieceCoordinate + (CANDIDATE_MOVE_OFFSET * advanceDirection);
        if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
            if (checkingMovesToUse.size() == 1  && !checkingPieceAttackPath.contains(candidateDestinationCoordinate)) {
                return;
            }
            if(checkingMovesToUse.isEmpty() && pinningPiecesOfPawnCoordinates.size() == 1){
                if(candidateDestinationCoordinate != pinningPiecesOfPawnCoordinates.get(0)){
                    return;
                }
            }
	        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
	        if (!candidateDestinationTile.isTileOccupied()) {
                if(currentRank != this.promotionRank){
	                pawnPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));//Add standard advance move
	                if (currentRank == this.initialRank) {
	                    addPotenialDoubleAdvanceMove(boardTiles,pawnPotentialLegalMoves, checkingMovesToUse, checkingPieceAttackPath, pinningPiecesOfPawnCoordinates);
	                }
	            }else
	                addPotentialPromotionMove(boardTiles,pawnPotentialLegalMoves, candidateDestinationCoordinate, checkingMovesToUse, checkingPieceAttackPath, pinningPiecesOfPawnCoordinates);
	        }
        }
    }
    
    private void addPotenialDoubleAdvanceMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, final Collection<Move> checkingMovesToUse, final List<Integer> checkingPieceAttackPath, final List<Integer> pinningPiecesOfPawnCoordinates) {
        int CandidateDoubleDestinationCoordinate = this.pieceCoordinate + (2 * CANDIDATE_MOVE_OFFSET * advanceDirection);
        if (checkingMovesToUse.size() == 1  && !checkingPieceAttackPath.contains(CandidateDoubleDestinationCoordinate)) {
            return;
        }
        if(checkingMovesToUse.isEmpty() && pinningPiecesOfPawnCoordinates.size() == 1){
            if(CandidateDoubleDestinationCoordinate != pinningPiecesOfPawnCoordinates.get(0)){
                return;
            }
        }
        final Tile candidateDestinationTile = boardTiles.get(CandidateDoubleDestinationCoordinate);
        if (!candidateDestinationTile.isTileOccupied())
            pawnPotentialLegalMoves.add(new PawnJumpMove(boardTiles,this.pieceCoordinate, CandidateDoubleDestinationCoordinate, this));
    }
    
    private void addPotentialPromotionMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, int candidateDestinationCoordinate, final Collection<Move> checkingMovesToUse, final List<Integer> checkingPieceAttackPath, final List<Integer> pinningPiecesOfPawnCoordinates) {
        if (checkingMovesToUse.size() == 1  && !checkingPieceAttackPath.contains(candidateDestinationCoordinate)) {
            return;
        }
        if(checkingMovesToUse.isEmpty() && pinningPiecesOfPawnCoordinates.size() == 1){
            if(candidateDestinationCoordinate != pinningPiecesOfPawnCoordinates.get(0)){
                return;
            }
        }
        pawnPotentialLegalMoves.add(new PawnPromotionMove(boardTiles, this.pieceCoordinate, candidateDestinationCoordinate, this));
    }
    
    private void addPotenialCaptureMoves(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, final Collection<Move> checkingMovesToUse, final List<Integer> checkingPieceAttackPath, final List<Integer> pinningPiecesOfPawnCoordinates) {
    	for (final int candidateOffset : CANDIDATE_CAPTURE_OFFSETS) {
            int candidateDestinationCoordinate = this.pieceCoordinate + (candidateOffset * advanceDirection);
            if (checkingMovesToUse.size() == 1  && !checkingPieceAttackPath.contains(candidateDestinationCoordinate)) {
                continue;
            }
            if(checkingMovesToUse.isEmpty() && pinningPiecesOfPawnCoordinates.size() == 1){
                if(candidateDestinationCoordinate != pinningPiecesOfPawnCoordinates.get(0)){
                    continue;
                }
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
                            addPotentialPromotionMove(boardTiles, pawnPotentialLegalMoves, candidateDestinationCoordinate, checkingMovesToUse, checkingPieceAttackPath, pinningPiecesOfPawnCoordinates);
                    }
	            }else if(currentRank == this.enPassantRank) {
	            	//check last move
	            	addPotentialEnPassantMove(boardTiles, pawnPotentialLegalMoves, candidateDestinationCoordinate, checkingMovesToUse, checkingPieceAttackPath, pinningPiecesOfPawnCoordinates);
	            }
            }
        }
    }
    
    private void addPotentialEnPassantMove(final List<Tile> boardTiles, final List<Move> pawnPotentialLegalMoves, int candidateDestinationCoordinate, final Collection<Move> checkingMovesToUse, final List<Integer> checkingPieceAttackPath, final List<Integer> pinningPiecesOfPawnCoordinates) {
        Move lastMove = GameHistory.getInstance().getLastMove();
        if (checkingMovesToUse.size() == 1  && !checkingPieceAttackPath.contains(candidateDestinationCoordinate)) {
            return;
        }
        if(checkingMovesToUse.isEmpty() && pinningPiecesOfPawnCoordinates.size() == 1){
            if(candidateDestinationCoordinate != pinningPiecesOfPawnCoordinates.get(0)){
                return;
            }
        }
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
