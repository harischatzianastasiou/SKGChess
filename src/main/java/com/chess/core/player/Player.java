package  com.chess.core.player;

import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;
import com.google.common.collect.ImmutableList;

public abstract class Player {
	protected final Collection<Piece> pieces;
	protected final Collection<Move> moves;
	protected final Alliance alliance;
	
	protected Player(final Collection<Piece> pieces,final Collection<Move> moves, final Alliance alliance) {
		this.pieces = pieces;
        this.moves = moves;
		this.alliance = alliance;
	}

	public static Player createPlayer(final List<Tile> tiles, final Alliance alliance,final Player opponentPlayer, String gameId) {
	  return opponentPlayer != null ? CurrentPlayer.createCurrentPlayer(tiles, alliance, opponentPlayer, gameId) : OpponentPlayer.createOpponentPlayer(tiles, alliance, gameId);
  }

	public Collection<Piece> getPieces(){
		return ImmutableList.copyOf(this.pieces);
	}
	
	public Collection<Move> getMoves() {
        return ImmutableList.copyOf(moves);
    }
	
	public Alliance getAlliance() {
		return this.alliance;
	}
}
