package  com.chess.core.moves.capturing;

import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.board.Board;
import  com.chess.core.board.IBoard;
import  com.chess.core.pieces.Pawn;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;

public class PawnPromotionCapturingMove extends CapturingMove {
    private final String promotionPieceType; // Store the piece type to promote to
    
    public PawnPromotionCapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove,capturedPiece);
        this.promotionPieceType = "QUEEN"; // Default to Queen for backward compatibility
    }
    
    public PawnPromotionCapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece, final String promotionPieceType) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove, capturedPiece);
        this.promotionPieceType = promotionPieceType; // Store the selected promotion piece type
    }
    
    @Override
    public IBoard execute() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();

        for (final Tile tile : super.getBoardTiles()) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                // Iterate over all current user pieces on the board
                if (!this.getPieceToMove().equals(piece) && this.getCapturedPiece().getPieceCoordinate() != piece.getPieceCoordinate()) {
                    builder.setPiece(piece);
                }
            }
        }

        // Use the stored promotion piece type instead of hardcoded value
        // This allows the frontend to specify which piece to promote to
        String newPieceType = this.promotionPieceType;

        // Create the promoted piece on the new board using the selected piece type
        Piece promotedPiece = ((Pawn) super.getPieceToMove()).promotePawn(super.getTargetCoordinate(), newPieceType);
        builder.setPiece(promotedPiece);

        // Set the next user's alliance
        builder.setcurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);

        return builder.build();
	    }
    
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PawnPromotionCapturingMove && super.equals(other);
    }
} 