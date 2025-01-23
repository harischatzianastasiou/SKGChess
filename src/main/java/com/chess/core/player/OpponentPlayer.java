package  com.chess.core.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.CalculateMoveUtils;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;
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

