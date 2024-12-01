package com.chess.engine.board.moves.nonCapturingMoves.castleMoves;

import com.chess.engine.board.moves.MoveResult;
import com.chess.engine.board.moves.nonCapturingMoves.NonCapturingMove;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.pieces.Rook;
import com.chess.engine.tiles.Tile;
import com.chess.engine.pieces.Piece;

import java.util.List;

public class QueenSideCastleMove extends NonCapturingMove {
    protected final int rookSourceCoordinate;
    protected final int rookTargetCoordinate;
    protected final Rook rook;

    public QueenSideCastleMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final int rookSourceCoordinate, final int rookTargetCoordinate, final Rook rook) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        this.rookSourceCoordinate = rookSourceCoordinate;
        this.rookTargetCoordinate = rookTargetCoordinate;
        this.rook = rook;
    }
    
@Override
	    public Board execute() {
	        final Board.Builder builder = new Board.Builder();
	        
	        for (final Tile tile : super.getBoardTiles()) {
	            if (tile.isTileOccupied()) {
	                final Piece piece = tile.getPiece();
	                if (!this.getPieceToMove().equals(piece) && !this.rook.equals(piece)) {
	                    builder.setPiece(piece);
	                }
	            }
	        }

	        // Move the king
	        final Piece movedKing = super.getPieceToMove().movePiece(this.getTargetCoordinate());
	        builder.setPiece(movedKing);

	        // Move the rook
	        final Piece movedRook = this.rook.movePiece(this.rookTargetCoordinate);
	        builder.setPiece(movedRook);

	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(super.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
	        
	        return builder.build();
	    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof QueenSideCastleMove && super.equals(other);
    }
} 