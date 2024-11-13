package com.chess.engine.player;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

public class OpponentPlayer extends Player  {
	
	private final Alliance alliance;

	public OpponentPlayer(Board board, Collection<Move> opponentMoves,Collection<Move> legalMoves,Alliance alliance) {
		super(board, opponentMoves, legalMoves);
		this.alliance = alliance;
	}
	
	public Collection<Piece> getActivePieces(){
		if(alliance == Alliance.WHITE) {
			return this.board.getWhitePieces();
		}else 
			return this.board.getBlackPieces();
	}
	
	public Alliance getAlliance() {
		return this.alliance;
	}
	
	public Alliance getOpponentAlliance() {
		return this.alliance; //fix
	}
}
