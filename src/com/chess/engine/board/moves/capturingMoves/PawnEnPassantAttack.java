package com.chess.engine.board.moves.capturingMoves;

import com.chess.engine.board.Board;
import com.chess.engine.board.tiles.Tile;
import com.chess.engine.pieces.Piece;

import java.util.List;

public class PawnEnPassantAttack extends CapturingMove {
    public PawnEnPassantAttack(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove, capturedPiece);
    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnEnPassantAttack && super.equals(other);
    }
}
