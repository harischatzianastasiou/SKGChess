package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.Tile.EmptyTile;
import com.google.common.collect.ImmutableMap;

public class BoardUtils {
	
	public static final int NUM_TILES = 64;
	public static final int NUM_TILES_PER_ROW = 8;
	public static final Map<Integer, Tile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();      // for caching create all possible empty tiles before using them.

	private BoardUtils() {
		throw new RuntimeException("Utility class, you cannot instantiate this utility class. Use it as a static member.}");
	}
	
	public static boolean isValidTileCoordinate(final int tileCoordinate) {
		return tileCoordinate >= 0 && tileCoordinate < NUM_TILES;
	}
	
    public static int getTileCoordinateRank(final int tileCoordinate) {
        return (tileCoordinate / NUM_TILES_PER_ROW);
    }

    public static int getTileCoordinateFile(final int tileCoordinate) {
        return tileCoordinate % NUM_TILES_PER_ROW ;
    }
	
	public static Map<Integer, Tile> createAllPossibleEmptyTiles(){
		final Map<Integer, Tile> emptyTileMap = new HashMap<>();
		for(int tileCoordinate=0; tileCoordinate<NUM_TILES; tileCoordinate++) {
	        Alliance alliance = (getTileCoordinateRank(tileCoordinate) + getTileCoordinateFile(tileCoordinate)) % 2 == 0 ? Alliance.BLACK : Alliance.WHITE;
			emptyTileMap.put(tileCoordinate,Tile.createTile(tileCoordinate, alliance, null));
		}
		return ImmutableMap.copyOf(emptyTileMap);//else if you don't want to use guava, use jdk's Collections.unmodifiableMap(emptyTileMap);
	}
}