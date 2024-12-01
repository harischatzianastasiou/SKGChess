package com.chess.model.moves;

import java.util.List;

import com.chess.model.board.Board;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public abstract class Move {
    final List<Tile> boardTiles;
    final int sourceCoordinate;
    final int targetCoordinate;
    private final Piece pieceToMove;

    public Move(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        this.boardTiles = boardTiles;    
        this.sourceCoordinate = sourceCoordinate;
        this.targetCoordinate = targetCoordinate;
        this.pieceToMove = pieceToMove;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.targetCoordinate;
        result = 31 * result + this.pieceToMove.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        final Move otherMove = (Move) other;
        return getSourceCoordinate() == otherMove.getSourceCoordinate() &&
               getTargetCoordinate() == otherMove.getTargetCoordinate() &&
               getPieceToMove().equals(otherMove.getPieceToMove());
    }
    
    public List<Tile> getBoardTiles() {
        return ImmutableList.copyOf(boardTiles);
    }
    
    public int getSourceCoordinate() {
        return sourceCoordinate;
    }
    
    public int getTargetCoordinate() {
        return targetCoordinate;
    }
    
    public Piece getPieceToMove() {
        return pieceToMove;
    }

    public abstract Piece getCapturedPiece();
        
    public abstract MoveResult simulate();

    public abstract Board execute();    
    
    public Board undo() {
        final Board.Builder builder = new Board.Builder();
        for (final Tile tile : this.boardTiles) {
            if (tile.isTileOccupied()) {
                builder.setPiece(tile.getPiece());
            }
        }        
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance());

        return builder.build();
    }
} 