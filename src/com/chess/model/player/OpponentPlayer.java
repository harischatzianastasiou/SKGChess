package com.chess.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.ProtectedCoordinatesTracker;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public final class OpponentPlayer extends Player {
		    
    private OpponentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance) {
        super(pieces, moves, alliance);
    }

	public static OpponentPlayer createOpponentPlayer(final List<Tile> tiles, final Alliance alliance) {
        final List<Piece> pieces = new ArrayList<>();
        final List<Move> moves = new ArrayList<>();
        ProtectedCoordinatesTracker.clear();

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

    @Override
    public boolean isCheckmate() {
        throw new UnsupportedOperationException("Opponent player cannot be checkmated");
    }

    @Override
    public boolean isInCheck() {
        throw new UnsupportedOperationException("Opponent player cannot be in check");
    }

    public static Collection<Move> getOpponentCheckingMoves(final List<Tile> tiles, final Alliance alliance, final Player opponentPlayer) {// moves that are checking the current player's king
        throw new UnsupportedOperationException("Opponent player should not calculate current player's checkiing moves");
    }
    public static int getKingCoordinate(final List<Tile> tiles, final Alliance alliance){
        throw new UnsupportedOperationException("Opponent player should not calculate his king position");
    }
}

