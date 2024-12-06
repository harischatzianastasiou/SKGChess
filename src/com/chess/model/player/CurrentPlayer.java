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
		
    private boolean isInCheck;
    private boolean isInCheckmate;
    
    private CurrentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance, boolean isInCheck, boolean isInCheckmate) {
        super(pieces, moves, alliance);
        this.isInCheck = isInCheck;
        this.isInCheckmate = isInCheckmate;
    }

	public static CurrentPlayer createCurrentPlayer(final List<Tile> tiles, final Alliance alliance,final Collection<Move> oppositePlayerMoves) {
        final List<Piece> activePieces = new ArrayList<>();
        final Collection<Move> potentialLegalMoves = new ArrayList<>();
        final Collection<Move> checkingMoves = new ArrayList<>();
        boolean isInCheck = false;
        boolean isInCheckmate = false;

        checkingMoves.addAll(checkingMoves(tiles, alliance, oppositePlayerMoves));
        if(!checkingMoves.isEmpty()){
            isInCheck = true;
        }
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                    potentialLegalMoves.addAll(piece.calculateCurrentPlayerMoves(tiles, checkingMoves, oppositePlayerMoves));
                }
            }
        }  

        if (isInCheck && potentialLegalMoves.isEmpty()) {
            isInCheckmate = true; 
        }
        return new CurrentPlayer(ImmutableList.copyOf(activePieces), ImmutableList.copyOf(potentialLegalMoves), alliance, isInCheck, isInCheckmate);    
    }

    public static Collection<Move> checkingMoves(final List<Tile> tiles, final Alliance alliance, final Collection<Move> oppositePlayerMoves) {// moves that are checking the current player's king
        Collection<Move> checkingMoves = new ArrayList<>();
        for (Move move : oppositePlayerMoves) {
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

    @Override
    public boolean isCheckmate() {
        return this.isInCheckmate;
    }

    @Override
    public boolean isInCheck() {
        return this.isInCheck;
    }

}