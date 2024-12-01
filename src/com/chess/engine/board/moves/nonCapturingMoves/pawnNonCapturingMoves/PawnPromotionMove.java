package com.chess.engine.board.moves.nonCapturingMoves.pawnNonCapturingMoves;

import com.chess.engine.board.moves.MoveResult;
import com.chess.engine.board.moves.nonCapturingMoves.NonCapturingMove;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.tiles.Tile;

import java.util.List;

public class PawnPromotionMove extends NonCapturingMove {
    public PawnPromotionMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        // add logic to promote pawn
    }
    
    @Override
    public Board execute() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();

        for (final Tile tile : super.getBoardTiles()) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                // Iterate over all current player pieces on the board
                if (!this.getPieceToMove().equals(piece)) {
                    builder.setPiece(piece);
                }
            }
        }

        // Let the user select a new piece
        System.out.println("Select a new piece for the pawn (Queen, Rook, Bishop, Knight): ");
        // Assume user input is handled elsewhere and stored in newPieceType
        String newPieceType = "QUEEN"; // Replace with actual user input

        // Create the promoted piece on the new board
        Piece promotedPiece = ((Pawn) super.getPieceToMove()).promotePawn(super.getTargetCoordinate(), newPieceType);
        builder.setPiece(promotedPiece);

        // Set the next player's alliance
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);

        return builder.build();
	    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnPromotionMove && super.equals(other);
    }
} 