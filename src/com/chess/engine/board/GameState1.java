package com.chess.engine.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.chess.engine.player.Player.*;
import com.google.common.collect.ImmutableList;

public class GameState1 {
	
	private final Collection<Move>  whiteLegalMoves;
	private final Collection<Move>  blackLegalMoves;
	private final Player whitePlayer;
	private final Player blackPlayer;
	
	public GameState(Board board) {
		this.whiteLegalMoves = calculateLegalMoves(board,Alliance.WHITE);
		this.blackLegalMoves = calculateLegalMoves(board,Alliance.BLACK);
		this.whitePlayer = new WhitePlayer(board,whiteLegalMoves,blackLegalMoves);
		this.blackPlayer = new BlackPlayer(board,blackLegalMoves,whiteLegalMoves);
	}
	
	private Collection<Move> calculateLegalMoves(Board board,Alliance alliance) {
		final List<Move> legalMoves = new ArrayList();
		for(final Tile tile : board.getTiles()){
				if(tile.isTileOccupied()){
					final Piece piece = tile.getPiece();
					if(piece.getPieceAlliance() == alliance){}
						legalMoves.addAll(piece.calculateLegalMoves(board));
				}	
		}
		return ImmutableList.copyOf(legalMoves);
	}
	
	public Player getWhitePlayer() {
		return this.whitePlayer;
	}
	
	public Player getBlackPlayer() {
        return this.blackPlayer;
    }
	
	
}
s