package com.chess.model.player;

import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public abstract class Player {
	protected final Collection<Piece>	pieces;
	protected final Collection<Move> moves;
	protected final Alliance alliance;
	
	protected Player(final Collection<Piece> pieces,final Collection<Move> moves, final Alliance alliance) {
		this.pieces = pieces;
        this.moves = moves;
		this.alliance = alliance;//could use incheck here?
	}

	public static Player createPlayer(final List<Tile> tiles, final Alliance alliance,final Collection<Move> oppositePlayerMoves) {
	  return oppositePlayerMoves != null ? CurrentPlayer.createCurrentPlayer(tiles, alliance, oppositePlayerMoves) : OpponentPlayer.createOpponentPlayer(tiles, alliance);
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
	
	public Alliance getOppositeAlliance() {
        return this.alliance == Alliance.WHITE ? Alliance.BLACK : Alliance.WHITE;
    }
	
	// public King getKing() {
	//     for (Piece piece : this.pieces) {
	//         if (piece instanceof King king && piece.getPieceAlliance() == alliance) {
	//             return king;
	//         }
	//     }
	//     throw new RuntimeException("No king found for this player");
	// }

	// public abstract boolean isCheckmate();
}
