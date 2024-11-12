package com.chess.engine.player;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

public class BlackPlayer extends Player  {

	public BlackPlayer(Board board, Collection<Move> blackStandardLegalMoves,Collection<Move> whiteStandardLegalMoves) {
		super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
	}

	public Collection<Piece> getActivePieces(){
		return board.getBlackPieces();
	}
	
	public Alliance getAlliance() {
		return Alliance.BLACK;
	}
	
	public Player getOpponent() {
		return this.board.getWhitePlayer();
	}
}
