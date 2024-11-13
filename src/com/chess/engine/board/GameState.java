package com.chess.engine.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.OpponentPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.CurrentPlayer;
import com.chess.engine.player.Player.*;
import com.google.common.collect.ImmutableList;

public class GameState {
	
	private final Collection<Move>  currentPlayerLegalMoves;
	private final Collection<Move>  opponentLegalMoves;
	private final Player currentPlayer;
	private final Player opponentPlayer;
	
	public GameState(Board board, Player moveMaker) {
		this.currentPlayerLegalMoves = calculateLegalMoves(moveMaker.);
		this.opponentLegalMoves = calculateLegalMoves(board,Alliance.BLACK);
		this.currentPlayer = new CurrentPlayer(board,currentPlayerLegalMoves,opponentLegalMoves,currentPlayerAlliance);
		this.opponentPlayer = new OpponentPlayer(board,opponentLegalMoves,currentPlayerLegalMoves,currentPlayerAlliance);
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
