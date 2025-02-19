package  com.chess.core.moves.noncapturing;

import  java.util.List;

import  com.chess.core.pieces.Piece;
import com.chess.core.tiles.Tile;

public class PawnJumpMove extends NonCapturingMove {
    public PawnJumpMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnJumpMove && super.equals(other);
    }
} 