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
	protected final Collection<Move> moves;
	private final Alliance alliance;
	
	private Player(final List<Tile> boardTiles,final Collection<Piece> pieces,final Collection<Move> moves, Alliance alliance) {
		this.boardTiles = boardTiles;
		this.pieces = pieces;
        this.moves = moves;
        this.alliance = alliance;
	}
	
	public static Player createPlayer(final List<Tile> boardTiles,final Collection<Piece> pieces,final Collection<Move> moves, Alliance alliance) {
		return new Player(boardTiles, pieces, moves, alliance);
	}

	public Collection<Piece> getPieces(){
		return this.pieces;
	}
	
	public Collection<Move> getMoves() {
        return this.moves;
    }
	
	public Alliance getAlliance() {
		return this.alliance;
	}
	
	public Alliance getOpponentAlliance() {
		if(this.alliance == Alliance.WHITE) {
			return Alliance.BLACK;
		}else 
			return Alliance.WHITE;
	}
}
