package com.chess.engine.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;

public class Player {
	private final List<Tile> boardTiles;
	private final Collection<Piece>	pieces;
	protected final Collection<Move> legalMoves;
	private final Alliance alliance;
	
	protected Player(final List<Tile> boardTiles,final Collection<Piece> pieces,final Collection<Move> legalMoves,final Alliance alliance) {
		this.boardTiles = boardTiles;
		this.pieces = pieces;
        this.legalMoves = legalMoves;
        this.alliance = alliance;
	}

	public Collection<Piece> getPieces(){
		return ImmutableList.copyOf(this.pieces);
	}
	
	public Collection<Move> getLegalMoves() {
        return ImmutableList.copyOf(legalMoves);
    }
	
	public Alliance getAlliance() {
		return this.alliance;
	}
	
	public Alliance getOpponentAlliance() {
        return this.alliance == Alliance.WHITE ? Alliance.BLACK : Alliance.WHITE;
    }

	public King getKing() {
	    for (Piece piece : this.pieces) {
	        if (piece instanceof King) {
	            return (King) piece;
	        }
	    }
	    throw new RuntimeException("No king found for this player");
	}

}
