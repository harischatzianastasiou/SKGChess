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

public abstract class Piece {

	protected final PieceSymbol pieceSymbol;
	protected final int pieceCoordinate;
	protected final Alliance pieceAlliance;
	private final boolean isFirstMove;
	private final int cachedHashCode;
	
	Piece(final PieceSymbol pieceSymbol, final int piecePosition,  final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceSymbol = pieceSymbol;
        this.pieceCoordinate = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }
	
	public PieceSymbol getPieceSymbol() {
		return pieceSymbol;
	}
	
	public int getPieceCoordinate() {
		return this.pieceCoordinate;     
	}
	
	public Alliance getPieceAlliance() {
		return this.pieceAlliance;
	}
	
	public boolean isFirstMove() {
		return isFirstMove;
	}
	
	public Collection<Move> calculateMovesConsideringOpponent(final List<Tile> boardTiles, final Collection<Move> opponentMoves, final boolean isKingInCheck, final int kingCoordinate) {
        final List<Move> legalMoves = new ArrayList<>();
        
        if (!isKingInCheck) {
            legalMoves.addAll(calculateMoves(boardTiles));
            // Filter out moves that put the King in check
            legalMoves.removeIf(move -> wouldMovePutKingInCheck( kingCoordinate, opponentMoves));
        } else {
            // Identify the pieces that threat the King
            List<Move> threatingCheckmateMoves = new ArrayList<>();
            for (Move move : opponentMoves) {
                if (move.getTargetCoordinate() == kingCoordinate) {
                	threatingCheckmateMoves.add(move);
                }
            }

            // If there's only one attacking piece, try to block or capture it
            if (threatingCheckmateMoves.size() == 1) {
                Move threatingCheckmateMove = threatingCheckmateMoves.get(0);
                int threatingPieceCoordinate = threatingCheckmateMove.getSourceCoordinate();

                // Calculate potential blocking moves
                legalMoves.addAll(calculateMoves(boardTiles));
                legalMoves.removeIf(move -> {
                    // Check if the move captures the attacking piece
                    if (move.getTargetCoordinate() == threatingPieceCoordinate) {
                        return false;
                    }
                    // Check if the move blocks the attack path
                    return !isMoveBlockingCheck(move, threatingCheckmateMove, kingCoordinate);
                });
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }
	
	public abstract Collection<Move> calculateMoves(final List<Tile> boardTiles);
	
	public abstract Piece movePiece(int destinationCoordinate);
	
	protected boolean wouldMovePutKingInCheck(final int kingCoordinate, final Collection<Move> opponentMoves ) {
	    // Check if the king's position or the pawn's target position is under attack
	    return isTileUnderAttack(kingCoordinate, opponentMoves);
	}
	
	protected boolean isTileUnderAttack(int coordinate, Collection<Move> opponentMoves) {
	    for (Move move : opponentMoves) {
	        if (move.getTargetCoordinate() == coordinate) {
	            return true;
	        }
	    }
	    return false;
	}
	
    protected boolean isMoveBlockingCheck(Move move, Move attackMove, int kingCoordinate) {
        int attackSource = attackMove.getSourceCoordinate();
        int attackTarget = attackMove.getTargetCoordinate();

        // Calculate the direction of the attack
        int rankDirection = Integer.signum(BoardUtils.getCoordinateRankDifference(attackSource, attackTarget));
        int fileDirection = Integer.signum(BoardUtils.getCoordinateFileDifference(attackSource, attackTarget));

        // Traverse the path from the attacker to the king
        int currentCoordinate = attackSource + rankDirection * 8 + fileDirection;
        while (currentCoordinate != kingCoordinate) {
            if (move.getTargetCoordinate() == currentCoordinate) {
                return true; // The move blocks the check
            }
            currentCoordinate += rankDirection * 8 + fileDirection;
        }

        return false; // The move does not block the check
    }
	
	@Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }
        final Piece otherPiece = (Piece) other;
        return this.pieceCoordinate == otherPiece.pieceCoordinate && this.pieceSymbol == otherPiece.pieceSymbol &&
               this.pieceAlliance == otherPiece.pieceAlliance && this.isFirstMove == otherPiece.isFirstMove;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    private int computeHashCode() {
        int result = this.pieceSymbol.hashCode();
        result = 31 * result + this.pieceAlliance.hashCode();
        result = 31 * result + this.pieceCoordinate;
        result = 31 * result + (this.isFirstMove ? 1 : 0);
        return result;
    }

	
	public enum PieceSymbol {
		PAWN("P"), 
		KNIGHT("N"),
		BISHOP("B"), 
		ROOK("R"), 
		QUEEN("Q"), 
		KING("K");
        
        private final String symbol;
        
        PieceSymbol(final String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return this.symbol;
        }
	}
}