package com.chess.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.board.validation.CastlingKingSideValidation;
import com.chess.model.board.validation.CastlingQueenSideValidation;
import com.chess.model.board.validation.MoveValidation;
import com.chess.model.board.validation.SelfNotOpenForCheckmateValidation;
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
        final List<Move> potentialLegalMoves = new ArrayList<>();
        boolean isInCheck = false;
        Collection<Move> checkingMoves = new ArrayList<>();

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if(piece instanceof King){
                    checkingMoves = checkingMoves(piece, oppositePlayerMoves);
                    if(!checkingMoves.isEmpty()){
                        isInCheck = true;
                    }
                }
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                    potentialLegalMoves.addAll(piece.calculatePotentialLegalMoves(tiles,checkingMoves,oppositePlayerMoves));
                }
            }
        }  

        for (Iterator<Move> iterator = potentialLegalMoves.iterator(); iterator.hasNext();) {
            Move move = iterator.next();
            MoveValidation moveValidation = new MoveValidation(List.of(new SelfNotOpenForCheckmateValidation(), new CastlingKingSideValidation(), new CastlingQueenSideValidation()));
            if (!moveValidation.validate(move, oppositePlayerMoves)) {
                iterator.remove();
            }
        } 

        Collection<Move> moves = ImmutableList.copyOf(potentialLegalMoves);

        return new CurrentPlayer(ImmutableList.copyOf(activePieces), ImmutableList.copyOf(moves), alliance, isInCheck);    
    }

    public static Collection<Move> checkingMoves(Piece piece, Collection<Move> oppositePlayerMoves) {
        Collection<Move> checkingMoves = new ArrayList<>();
        for (Move move : oppositePlayerMoves) {
            if (move.getTargetCoordinate() == piece.getPieceCoordinate()) {
                checkingMoves.add(move);
            }
        }
        return checkingMoves;
    }

    public final boolean isCheckmate() {
        return this.isInCheck && this.getMoves().isEmpty();
    }
}