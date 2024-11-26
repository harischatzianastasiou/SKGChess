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

    public static Player createPlayer(final List<Tile> tiles, Alliance alliance, final boolean isInCheck) {
        final List<Piece> activePieces = new ArrayList<>();
        final List<Move> legalMoves = new ArrayList<>();
//        final List<Move> opponentMoves = calculateOpponentMoves(tiles, alliance);
        final int kingCoordinate = getKingCoordinate(tiles, alliance);
        final int oppositeKingCoordinate = getOppositeKingCoordinate(tiles, alliance);
        final int[] oppositeKingSideCastlePath = getOppositeKingSideCastlingPath(oppositeKingCoordinate, alliance);
        final int[] oppositeQueenSideCastlePath = getOppositeQueenSideCastlingPath(oppositeKingCoordinate, alliance);
        
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                	activePieces.add(piece);
                    legalMoves.addAll(piece.calculateMoves(tiles, isInCheck, oppositeKingCoordinate, oppositeKingSideCastlePath, oppositeQueenSideCastlePath));
                }
            }
        }
		if(isInCheck && legalMoves.isEmpty()) {
			System.out.println("Checkmate");
		}
	    if(isInCheck && legalMoves.isEmpty()) {
	        System.out.println("Stalemate");
	    }        
        return new Player(tiles, ImmutableList.copyOf(activePieces), ImmutableList.copyOf(legalMoves), alliance, isInCheck);
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
	
//	public static boolean isKingInCheck(final int kingCoordinate, final List<Move> opponentMoves) { 
//		for (Move opponentMove : opponentMoves) {
//			if(opponentMove.getTargetCoordinate() == kingCoordinate) {
//				return true;
//	    	}
//		}
//    return false;
//	}
	
	public static int getOppositeKingCoordinate(final List<Tile> tiles, final Alliance alliance) {
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
    
    private static int[] getOppositeKingSideCastlingPath(int kingCoordinate, Alliance kingAlliance) {
        // Assuming standard chess board coordinates
        // Define the kingside castling paths for both white and black
        int[] kingsideCastlingPath;

        if (kingAlliance.isWhite()) {
            // White king's kingside castling path
            kingsideCastlingPath = new int[]{61, 62}; // f1, g1
        } else {
            // Black king's kingside castling path
            kingsideCastlingPath = new int[]{5, 6}; // f8, g8
        }

        // Return the kingside castling path
        return kingsideCastlingPath;
    }
    
    private static int[] getOppositeQueenSideCastlingPath(int kingCoordinate, Alliance kingAlliance) {
        // Assuming standard chess board coordinates
        // Define the queenside castling paths for both white and black
        int[] queensideCastlingPath;

        if (kingAlliance.isWhite()) {
            // White king's queenside castling path
            queensideCastlingPath = new int[]{59, 58, 57}; // c1, d1, b1
        } else {
            // Black king's queenside castling path
            queensideCastlingPath = new int[]{3, 2, 1}; // c8, d8, b8
        }

        // Return the queenside castling path
        return queensideCastlingPath;
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