package com.chess.engine.board;

public class Board {

	public Tile getTile(final int candidateDestinationCoordinate) {
		return null;
	}
	
	public static boolean isValidTile(final int tileCoordinate) {
		
		boolean isValidTile = false;
		
		if(tileCoordinate >= 0 && tileCoordinate < 64)
			isValidTile = true;
		
        return isValidTile;
	}
	
	

}
