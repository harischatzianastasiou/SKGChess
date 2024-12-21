package com.chess.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public final class CurrentPlayer extends Player {
		
    private final boolean isInCheck;
    private final boolean isInCheckmate;
    
    private CurrentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance, boolean isInCheck, boolean isInCheckmate) {
        super(pieces, moves, alliance);
        this.isInCheck = isInCheck;
        this.isInCheckmate = isInCheckmate;
    }

	public static CurrentPlayer createCurrentPlayer(final List<Tile> tiles, final Alliance alliance,final Player opponentPlayer) {
        final List<Piece> pieces = new ArrayList<>();
        final Collection<Move> moves = new ArrayList<>();
        final Collection<Move> opponentCheckingMoves = new ArrayList<>();
        boolean isInCheck = false;
        boolean isInCheckmate = false;

        opponentCheckingMoves.addAll(getOpponentCheckingMoves(tiles, alliance, opponentPlayer));
        if(!opponentCheckingMoves.isEmpty()){
            isInCheck = true;
        }
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    pieces.add(piece);
                    moves.addAll(piece.calculateMoves(tiles, opponentPlayer));
                }
            }
        }  

        if (isInCheck && moves.isEmpty()) {
            isInCheckmate = true; 
        }
        return new CurrentPlayer(ImmutableList.copyOf(pieces), ImmutableList.copyOf(moves), alliance, isInCheck, isInCheckmate);    
    }

    public static Collection<Move> getOpponentCheckingMoves(final List<Tile> tiles, final Alliance alliance, final Player opponentPlayer) {// moves that are checking the current player's king
        Collection<Move> checkingMoves = new ArrayList<>();
        for (Move move : opponentPlayer.getMoves()) {
            if (move.getTargetCoordinate() == getKingCoordinate(tiles, alliance)) {
                checkingMoves.add(move);
            }
        }
        return checkingMoves;
    }

    public static int getKingCoordinate(final List<Tile> tiles, final Alliance alliance) {
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    if (piece instanceof King && piece.getPieceAlliance() == alliance) {
                        return piece.getPieceCoordinate();
                    }
                }
            }
        }
        throw new RuntimeException("No king found for this player");
    }

    public boolean isCheckmate() {
        return this.isInCheckmate;
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

}