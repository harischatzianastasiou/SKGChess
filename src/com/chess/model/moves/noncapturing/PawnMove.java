package com.chess.model.moves.noncapturing;

import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;

import java.util.List;

public class PawnMove extends NonCapturingMove {
    public PawnMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnMove && super.equals(other);
    }
} 