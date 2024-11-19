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

    public static Player createPlayer(List<Tile> tiles, Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        final List<Move> legalMoves = new ArrayList<>();
        final List<Move> opponentMoves = calculateOpponentMoves(tiles, alliance);

        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                	activePieces.add(piece);
                	for (Move move : piece.calculateMoves(tiles)) {
                		if (isMoveLegal(piece, move, opponentMoves, tiles, alliance)) {
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }
        return new Player(tiles, ImmutableList.copyOf(activePieces), ImmutableList.copyOf(legalMoves), alliance);
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
    
    private static boolean isMoveLegal(Piece piece, Move move, List<Move> opponentMoves, List<Tile> tiles, Alliance alliance) {
    	if(piece instanceof King) {
	    	for(final Move opponentMove : opponentMoves){
	    		if(opponentMove.getTargetCoordinate() == move.getTargetCoordinate()) {
					return false;
				}
	    	}
        return true; // Replace with actual legality check logic
    }
	
	public boolean hasCurrentPlayerKingEscapeMoves() {
	    for (final Move legalMove : this.currentPlayer.getMoves()) {
	        if (legalMove.getMovedPiece() == this.currentPlayerKing) {
	            boolean isSafeMove = true;
	            for (final Move opponentMove : this.opponentPlayer.getMoves()) {
	                if (opponentMove.getTargetCoordinate() == legalMove.getTargetCoordinate()) {
	                    isSafeMove = false;
	                    break;
	                }
	            }
	            if (isSafeMove) {
	                return true; // The King has at least one escape move
	            }
	        }
	    }
	    return false; // No escape moves for the King
	}
	
	public boolean isInCheckMate() {
	    return this.isCurrentPlayerKingInCheck && !this.hasCurrentPlayerKingEscapeMoves();
	}

	public boolean isInStaleMate() {
	    return !this.isCurrentPlayerKingInCheck && !this.hasCurrentPlayerKingEscapeMoves();
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