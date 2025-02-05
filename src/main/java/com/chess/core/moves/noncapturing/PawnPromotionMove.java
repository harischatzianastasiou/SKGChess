package  com.chess.core.moves.noncapturing;

import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.board.Board;
import  com.chess.core.board.IBoard;
import  com.chess.core.pieces.Pawn;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;

public class PawnPromotionMove extends NonCapturingMove {
    public PawnPromotionMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        // add logic to promote pawn
    }
    
    @Override
    public IBoard execute() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();

        for (final Tile tile : super.getBoardTiles()) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                // Iterate over all current user pieces on the board
                if (!this.getPieceToMove().equals(piece)) {
                    builder.setPiece(piece);
                }
            }
        }

        // Let the user select a new piece
        // Assume user input is handled elsewhere and stored in newPieceType
        String newPieceType = "QUEEN"; // Replace with actual user input

        // Create the promoted piece on the new board
        Piece promotedPiece = ((Pawn) super.getPieceToMove()).promotePawn(super.getTargetCoordinate(), newPieceType);
        builder.setPiece(promotedPiece);

        // Set the next user's alliance
        builder.setcurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);

        return builder.build();
	    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnPromotionMove && super.equals(other);
    }
} 