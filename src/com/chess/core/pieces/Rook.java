package  com.chess.core.pieces;

import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.tiles.Tile;
import  com.chess.core.player.Player;

public class Rook extends Piece {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -8, -1, 1, 8 };
	
	public Rook(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.ROOK, pieceCoordinate, pieceAlliance, true);
	}
	
	public Rook(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.ROOK, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
	
	@Override
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final Player opponentPlayer) {
		return CalculateMoveUtils.calculate(boardTiles, this, CANDIDATE_MOVE_OFFSETS, opponentPlayer);
	} 
        
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new Rook(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
