package com.chess.model.moves.noncapturing;

import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.board.Board;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Rook;
import com.chess.model.tiles.Tile;

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
	public IBoard execute() {
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