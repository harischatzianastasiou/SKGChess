package  com.chess.core.moves.noncapturing;

import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.board.Board;
import  com.chess.core.board.Board.Builder;
import  com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;

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
            
        // Set the next user's alliance
        builder.setcurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
        return builder;
    }


    @Override
    public IBoard execute() {
        return this.createBuilderAfterNonCapturingMove().build();
    }
} 