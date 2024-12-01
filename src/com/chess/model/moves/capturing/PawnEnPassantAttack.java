package com.chess.model.moves.capturing;

import java.util.List;

import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;

public class PawnEnPassantAttack extends CapturingMove {
    public PawnEnPassantAttack(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove, capturedPiece);
    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnEnPassantAttack && super.equals(other);
    }
}
