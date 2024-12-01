package com.chess.engine.board.moves.nonCapturingMoves.pawnNonCapturingMoves;

import com.chess.engine.board.moves.nonCapturingMoves.NonCapturingMove;
import com.chess.engine.pieces.Piece;
import com.chess.engine.board.Tile;

import java.util.List;

public class PawnJumpMove extends NonCapturingMove {
    public PawnJumpMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnJumpMove && super.equals(other);
    }
} 