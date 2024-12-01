package com.chess.model.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public class Player {
	private final Collection<Piece>	pieces;
	protected final Collection<Move> potentialLegalMoves;
	private final Alliance alliance;
	
	protected Player(final Collection<Piece> pieces,final Collection<Move> potentialLegalMoves,final Alliance alliance) {
		this.pieces = pieces;
        this.potentialLegalMoves = potentialLegalMoves;
        this.alliance = alliance;
	}

	public static Player createPlayer(final List<Tile> tiles, Alliance alliance) {
		final List<Piece> activePieces = new ArrayList<>();
		final List<Move> potentialLegalMoves = new ArrayList<>();
	  
	  for (final Tile tile : tiles) {
		  if (tile.isTileOccupied()) {
			  final Piece piece = tile.getPiece();
			  if (piece.getPieceAlliance() == alliance) {
				  activePieces.add(piece);
				  potentialLegalMoves.addAll(piece.calculatePotentialLegalMoves(tiles));
			  }
		  }
	  }      
	  return new Player(ImmutableList.copyOf(activePieces), ImmutableList.copyOf(potentialLegalMoves), alliance);
  }

	public Collection<Piece> getPieces(){
		return ImmutableList.copyOf(this.pieces);
	}
	
	public Collection<Move> getPotentialLegalMoves() {
        return ImmutableList.copyOf(potentialLegalMoves);
    }
	
	public Alliance getAlliance() {
		return this.alliance;
	}
	
	public Alliance getOpponentAlliance() {
        return this.alliance == Alliance.WHITE ? Alliance.BLACK : Alliance.WHITE;
    }

	public King getKing() {
	    for (Piece piece : this.pieces) {
	        if (piece instanceof King king) {
	            return king;
	        }
	    }
	    throw new RuntimeException("No king found for this player");
	}
}
