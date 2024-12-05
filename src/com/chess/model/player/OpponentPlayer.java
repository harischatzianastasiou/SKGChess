package com.chess.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public final class OpponentPlayer extends Player {
		    
    private OpponentPlayer(final Collection<Piece> pieces, final Collection<Move> potentialLegalMoves, final Alliance alliance) {
        super(pieces, potentialLegalMoves, alliance);
    }

	public static OpponentPlayer createOpponentPlayer(final List<Tile> tiles, final Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        final List<Move> potentialLegalMoves = new ArrayList<>();

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                    potentialLegalMoves.addAll(piece.calculatePotentialLegalMoves(tiles,null,null));
                }
            }
        }   
        return new OpponentPlayer(ImmutableList.copyOf(activePieces), ImmutableList.copyOf(potentialLegalMoves), alliance);    
    }

    // @Override
    // public boolean isCheckmate() {
    //     return false;
    // }
}

