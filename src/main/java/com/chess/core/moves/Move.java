package  com.chess.core.moves;

import java.util.List;

import  com.chess.core.board.IBoard;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;
import com.google.common.collect.ImmutableList;

public abstract class Move {
    final List<Tile> boardTiles;
    final int sourceCoordinate;
    final int targetCoordinate;
    private final Piece pieceToMove;

    public Move(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        this.boardTiles = boardTiles;    
        this.sourceCoordinate = sourceCoordinate;
        this.targetCoordinate = targetCoordinate;
        this.pieceToMove = pieceToMove;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.targetCoordinate;
        result = 31 * result + this.pieceToMove.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        final Move otherMove = (Move) other;
        return getSourceCoordinate() == otherMove.getSourceCoordinate() &&
               getTargetCoordinate() == otherMove.getTargetCoordinate() &&
               getPieceToMove().equals(otherMove.getPieceToMove());
    }
    
    public List<Tile> getBoardTiles() {
        return ImmutableList.copyOf(boardTiles);
    }
    
    public int getSourceCoordinate() {
        return sourceCoordinate;
    }
    
    public int getTargetCoordinate() {
        return targetCoordinate;
    }
    
    public Piece getPieceToMove() {
        return pieceToMove;
    }

    public abstract Piece getCapturedPiece();

    public abstract IBoard execute(String gameId);
} 