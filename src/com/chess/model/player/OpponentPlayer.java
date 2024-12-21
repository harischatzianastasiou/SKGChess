package com.chess.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public final class OpponentPlayer extends Player {
		    
    private OpponentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance) {
        super(pieces, moves, alliance);
    }

	public static OpponentPlayer createOpponentPlayer(final List<Tile> tiles, final Alliance alliance) {
        final List<Piece> pieces = new ArrayList<>();
        final List<Move> moves = new ArrayList<>();
        CalculateMoveUtils.ProtectedCoordinatesTracker.clear(); // every time an opponent player calculates his moves we clear the ProtectedCoordinatesTracker

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                    pieces.add(piece);
                    moves.addAll(piece.calculateMoves(tiles, null));
                }
            }
        }   
        return new OpponentPlayer(ImmutableList.copyOf(pieces), ImmutableList.copyOf(moves), alliance);    
    }
}

