package com.chess.engine.player;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;

public class BlackPlayer extends Player  {

	public BlackPlayer(Board board, Collection<Move> blackStandardLegalMoves,Collection<Move> whiteStandardLegalMoves) {
		super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
	}

}
