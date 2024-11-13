package com.chess.engine.player;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

public class CurrentPlayer extends Player  {
	
	private final Alliance alliance;

	public CurrentPlayer(Board board, Collection<Move> legalMoves,Collection<Move> opponentMoves, Alliance alliance) {
		super(board, legalMoves, opponentMoves);
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
