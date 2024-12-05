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
    
    private CurrentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance, final boolean isInCheck) {
        super(pieces, moves, alliance);
        this.isInCheck = isInCheck;
    }

	public static CurrentPlayer createCurrentPlayer(final List<Tile> tiles, final Alliance alliance,final Collection<Move> oppositePlayerMoves) {
        final List<Piece> activePieces = new ArrayList<>();
        Collection<Move> potentialLegalMoves = new ArrayList<>();
        boolean isInCheck = false;
        boolean isInCheckmate = false;
        Collection<Move> paok = new ArrayList<>();

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if(piece instanceof King && piece.getPieceAlliance() == alliance){
                    final Collection<Move> checkingMoves = checkingMoves(tile, oppositePlayerMoves);
                    paok = ImmutableList.copyOf(checkingMoves);
                    System.out.println("PAAAAAAAAAAAAAAAAAOOOOOOOOOOOOOOOOOOKKKKKKKKKKKKKKKcheckingMoves: " + checkingMoves);
                    if(!checkingMoves.isEmpty()){
                        isInCheck = true;
                    }
                }
            }
        }
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                    System.out.println("Calculating moves for piece at " + piece.getPieceCoordinate() + 
                                      " with checking moves: " + paok);
                    potentialLegalMoves.addAll(piece.calculatePotentialLegalMoves(tiles,paok,oppositePlayerMoves));
                }
            }
        }  

        
        if (isInCheck && (potentialLegalMoves == null || potentialLegalMoves.isEmpty())) {
            isInCheckmate = true;
        }
        return new CurrentPlayer(ImmutableList.copyOf(activePieces), ImmutableList.copyOf(potentialLegalMoves), alliance, isInCheck);    
    }

    public static Collection<Move> checkingMoves(Tile tile, Collection<Move> oppositePlayerMoves) {// moves that are checking the current player's king
        Collection<Move> checkingMoves = new ArrayList<>();
        for (Move move : oppositePlayerMoves) {
            if (move.getTargetCoordinate() == tile.getTileCoordinate()) {
                checkingMoves.add(move);
            }
        }
        return checkingMoves;
    }

    // @Override
    // public final boolean isCheckmate() {
    //     return this.isInCheckmate;
    // }
}