package  com.chess.core.moves.capturing;

import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.board.Board;
import  com.chess.core.board.Board.Builder;
import  com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;

public class CapturingMove extends Move {
    private final Piece capturedPiece;

    public CapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        this.capturedPiece = capturedPiece;
    }
    
    @Override
    public Piece getCapturedPiece() {
        return this.capturedPiece;
    }

    public Builder createBuilderAfterCapturingMove() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();
            
        for (final Tile tile : super.getBoardTiles()) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if(!this.getPieceToMove().equals(piece) && this.getCapturedPiece().getPieceCoordinate() != piece.getPieceCoordinate()) {
                    builder.setPiece(piece);
                }
            }   
        }

        // Create the moved piece on the new board
        Piece movedPiece = super.getPieceToMove().movePiece(super.getTargetCoordinate());
        builder.setPiece(movedPiece);
            
        // Set the next user's alliance
        builder.setcurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
        return builder;
    }

    @Override
    public IBoard execute() {
        return this.createBuilderAfterCapturingMove().build();
    }
} 