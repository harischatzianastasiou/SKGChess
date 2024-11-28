package com.chess.engine.player;

import java.util.ArrayList;
import java.util.List;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;

public class PlayerFactory {
	
    public static Player createPlayer(final List<Tile> tiles, Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        final List<Move> legalMoves = new ArrayList<>();
        
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                	activePieces.add(piece);
                    legalMoves.addAll(piece.calculateMoves(tiles, alliance));
                }
            }
        }      
        return new Player(ImmutableList.copyOf(activePieces), ImmutableList.copyOf(legalMoves), alliance);
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
}
