package com.chess.engine.player;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

public class WhitePlayer extends Player  {

	public WhitePlayer(Board board, Collection<Move> whiteStandardLegalMoves,Collection<Move> blackStandardLegalMoves) {
		super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
	}
	
	public Collection<Piece> getActivePieces(){
		return board.getWhitePieces();
	}
	
	public Alliance getAlliance() {
		return Alliance.WHITE;
	}
	
	public Player getOpponent() {
		return this.board.getBlackPlayer();
	}
}
