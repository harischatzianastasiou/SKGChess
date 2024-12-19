package com.chess.model.pieces;

import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public abstract class Piece {

	protected final PieceSymbol pieceSymbol;
	protected final int pieceCoordinate;
	protected final Alliance pieceAlliance;
	private final boolean isFirstMove;
	private final int cachedHashCode;
		
	Piece(final PieceSymbol pieceSymbol, final int pieceCoordinate,  final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceSymbol = pieceSymbol;
        this.pieceCoordinate = pieceCoordinate;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
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

    public abstract Collection<Move> calculateMoves(final List<Tile> boardTiles, final Player opponentPlayer);

	public abstract Piece movePiece(int destinationCoordinate);
}




// 1. Code Duplication: There is some code duplication, especially in the calculatePotentialLegalMoves methods across different piece classes. Consider using a more generic approach or utility methods to reduce redundancy.
// 2. Magic Numbers: There are several magic numbers, such as -9, -7, 7, 9 in the piece classes. These should be replaced with named constants to improve readability.
// 3. User Input Handling: The PawnPromotionMove class assumes user input for pawn promotion, which is not implemented. This could lead to runtime errors. Consider implementing a proper user input mechanism or interface.
// 4. Testing: There is no mention of unit tests. Given the complexity of a chess game, comprehensive testing is crucial to ensure correctness. Consider adding unit tests for critical components and game scenarios.
// 5. Performance Considerations: The use of new keyword in move execution (e.g., creating new Board and Piece objects) could be optimized. Consider using object pooling or other techniques if performance becomes an issue.	

// Design Patterns: While the code uses some design patterns effectively, there might be opportunities to apply others, such as the Strategy pattern for move validation or the Factory pattern for piece creation.
