package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.GameHistory;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerFactory {

    public static Player createPlayer(final List<Tile> tiles, Alliance alliance,boolean isInitialSetup) {
        final List<Piece> activePieces = new ArrayList<>();
        final List<Move> legalMoves = new ArrayList<>();
        final List<Move> opponentMoves = calculateOpponentMoves(tiles, alliance);
        final int kingCoordinate = getKingCoordinate(tiles, alliance);
        boolean isKingInCheck = isKingInCheck(kingCoordinate,opponentMoves);

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                	activePieces.add(piece);
                	if(isInitialSetup) {
                        legalMoves.addAll(piece.calculateMoves(tiles, isKingInCheck));
                    } else {
                        legalMoves.addAll(piece.calculateMovesConsideringOpponent(tiles, opponentMoves, isKingInCheck, kingCoordinate));
                    }
                }
            }
        }
		if(isKingInCheck && legalMoves.isEmpty()) {
			System.out.println("Checkmate");
		}
	    if(isKingInCheck && legalMoves.isEmpty()) {
	        System.out.println("Stalemate");
	    }        	
        return new Player(tiles, ImmutableList.copyOf(activePieces), ImmutableList.copyOf(legalMoves), alliance, isKingInCheck);
    }
   
    
    private static List<Move> calculateOpponentMoves(final List<Tile> tiles, final Alliance alliance) {
        final List<Move> opponentMoves = new ArrayList<>();
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() != alliance) {
                    opponentMoves.addAll(piece.calculateMoves(tiles));
                }
            }
        }
        return ImmutableList.copyOf(opponentMoves);
    }
		
	public static int getKingCoordinate(final List<Tile> tiles, final Alliance alliance) {
	    for (Tile tile : tiles) {
	        if (tile.isTileOccupied()) {
	            Piece piece = tile.getPiece();
	            if (piece instanceof King && piece.getPieceAlliance() == alliance) {
	                return tile.getTileCoordinate();
	            }
	        }
	    }
	    throw new RuntimeException("King not found on the board");
	}
	
	public static boolean isKingInCheck(final int kingCoordinate, final List<Move> opponentMoves) { 
		for (Move opponentMove : opponentMoves) {
			if(opponentMove.getTargetCoordinate() == kingCoordinate) {
				return true;
	    	}
		}
    return false;
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