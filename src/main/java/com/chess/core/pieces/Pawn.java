package  com.chess.core.pieces;

import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;

public class Pawn extends Piece {
    
    private static final int[] CANDIDATE_MOVE_OFFSETS = {7, 8, 9, 16};
    private final int advanceDirection;
    private final int initialRank;
    private final int currentRank;
    private final int promotionRank;
    private final int enPassantRank;
    
    public int getAdvanceDirection() {
        return advanceDirection;
    }

    public int getInitialRank() {
        return initialRank;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    public int getPromotionRank() {
        return promotionRank;
    }

    public int getEnPassantRank() {
        return enPassantRank;
    }

    public Pawn(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.PAWN,pieceCoordinate, pieceAlliance, true);
        this.advanceDirection = this.pieceAlliance.getMovingDirection();  
        this.initialRank = this.pieceAlliance.isWhite() ? 7: 2;
        this.currentRank = CalculateMoveUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.enPassantRank = this.pieceAlliance.isWhite()? 4 : 5;
    }
    
    public Pawn(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.PAWN,pieceCoordinate, pieceAlliance, isFirstMove);
        this.advanceDirection = this.pieceAlliance.getMovingDirection();  
        this.initialRank = this.pieceAlliance.isWhite() ? 7 : 2;
        this.currentRank = CalculateMoveUtils.getCoordinateRank(this.pieceCoordinate);
        this.promotionRank = this.pieceAlliance.isWhite() ? 2 : 7;
        this.enPassantRank = this.pieceAlliance.isWhite()? 4 : 5;
    }
    
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
     
	@Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final Player opponentPlayer, final Move lastMove) {
		return CalculateMoveUtils.calculate(boardTiles, this, CANDIDATE_MOVE_OFFSETS, opponentPlayer, lastMove);
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
}
