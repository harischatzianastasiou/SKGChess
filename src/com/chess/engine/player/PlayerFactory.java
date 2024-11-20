package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class PlayerFactory {

    public static Player createPlayer(final List<Tile> tiles, final Alliance alliance,final boolean isInCheck) {
        final List<Piece> activePieces = new ArrayList<>();
        final List<Move> legalMoves = new ArrayList<>();
        final List<Move> opponentMoves = calculateOpponentMoves(tiles, alliance);

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                	activePieces.add(piece);
            		for (Move move : piece.calculateMoves(tiles)) {
            			if(piece instanceof King) {
	            			for (Move opponentMove : opponentMoves) {
	    	                	if (!leavesKingInCheck(move, opponentMove)) {
	    	                		legalMoves.add(move);
	    	                    }
	            			}
            			}
            			else
            				legalMoves.add(move);
            		}
                }
            }
        }
		if(isInCheck && legalMoves.isEmpty()) {
			System.out.println("Checkmate");
		}
	    if(isInCheck && legalMoves.isEmpty()) {
	        System.out.println("Stalemate");
	    }        	
        return new Player(tiles, ImmutableList.copyOf(activePieces), ImmutableList.copyOf(legalMoves), alliance, false);
    }
   
    
    private static List<Move> calculateOpponentMoves(List<Tile> tiles, Alliance alliance) {
        final List<Move> opponentMoves = new ArrayList<>();
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() != alliance) {
                    opponentMoves.addAll(piece.calculateMoves(tiles));
                }
            }
        }
        return opponentMoves;
    }
    
	public static boolean leavesKingInCheck(Move move, Move opponentMove) { 
		if(opponentMove.getTargetCoordinate() == move.getTargetCoordinate()) {
			return true;
    	}
        return false; // Replace with actual legality check logic
	}
	
	public boolean isThreefoldRepetition() {
        return false; //TODO implement threefold repetition detection
    }
	
	public boolean isFiftyMoveRule() {
        return false; //TODO implement fifty move rule detection
    }
	
	public boolean isCastled() {
		return false; //TODO implement castling detection
	}
	
	public boolean isEnPassantLegal() {
        return false; //TODO implement en passant detection
    }
}