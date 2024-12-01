package com.chess.model.board;

import com.chess.model.Alliance;

public class BoardUtils {
	
	public static final int NUM_TILES = 64;
	public static final int NUM_TILES_PER_ROW = 8;

	private BoardUtils() {
		throw new RuntimeException("Utility class, you cannot instantiate this utility class. Use it as a static member.}");
	}
	
	public static boolean isValidTileCoordinate(final int tileCoordinate) {
		return tileCoordinate >= 0 && tileCoordinate < NUM_TILES;
	}
	
    public static int getCoordinateRank(int tileCoordinate) {
        return (Math.abs(tileCoordinate) / NUM_TILES_PER_ROW) + 1;
    }

    public static int getCoordinateFile(int tileCoordinate) {
        return (Math.abs(tileCoordinate) % NUM_TILES_PER_ROW) + 1;
    }
    
    public static int getCoordinateRankDifference(int destinationCoordinate, int sourceCoordinate) {
        return Math.abs(getCoordinateRank(destinationCoordinate)) - Math.abs(getCoordinateRank(sourceCoordinate));
    }

    public static int getCoordinateFileDifference(int destinationCoordinate, int sourceCoordinate) {
        return Math.abs(getCoordinateFile(destinationCoordinate)) - Math.abs(getCoordinateFile(sourceCoordinate));
    }
    
    public static Alliance getCoordinateAlliance(int tileCoordinate) {
        return (BoardUtils.getCoordinateRank(tileCoordinate) + BoardUtils.getCoordinateFile(tileCoordinate)) % 2 == 0 ? Alliance.BLACK : Alliance.WHITE;
    }
}