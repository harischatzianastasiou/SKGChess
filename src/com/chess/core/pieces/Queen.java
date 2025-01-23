package  com.chess.core.pieces;

import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.tiles.Tile;
import  com.chess.core.player.Player;

public class Queen extends Piece {
    
    private static final int[] QUEEN_MOVE_OFFSETS = { -8, -1, 1, 8, -9, -7, 7, 9 };

    public Queen(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.QUEEN, pieceCoordinate, pieceAlliance, true);
    }

    public Queen(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.QUEEN, pieceCoordinate, pieceAlliance, isFirstMove);
    }

    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }

	@Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final Player opponentPlayer) {
		return CalculateMoveUtils.calculate(boardTiles, this, QUEEN_MOVE_OFFSETS, opponentPlayer);
	} 

    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Queen(destinationCoordinate, this.getPieceAlliance(), false);
    }
}
