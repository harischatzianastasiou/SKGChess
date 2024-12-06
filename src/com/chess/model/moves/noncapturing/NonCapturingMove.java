package com.chess.model.moves.noncapturing;

import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.board.Board;
import com.chess.model.board.Board.Builder;
import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;

public class NonCapturingMove extends Move {
    public NonCapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
    }
    
    @Override
    public Piece getCapturedPiece() {
        return null;
    }
    public Builder createBuilderAfterNonCapturingMove() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();
            
        for (final Tile tile : super.getBoardTiles()) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if(!this.getPieceToMove().equals(piece)) {
                    builder.setPiece(piece);
                }
            }   
        }

        // Create the moved piece on the new board
        Piece movedPiece = super.getPieceToMove().movePiece(super.getTargetCoordinate());
        builder.setPiece(movedPiece);
            
        // Set the next player's alliance
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
        return builder;
    }


    @Override
    public IBoard execute() {
        return this.createBuilderAfterNonCapturingMove().build();
    }
} 