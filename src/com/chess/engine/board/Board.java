package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.google.common.collect.ImmutableMap;

public class Board {
	
	private Map<Integer, Tile> ALL_TILES = new HashMap<> (BoardUtils.createAllPossibleEmptyTiles());

	public Tile getTile(final int tileCoordinate) {
        return ALL_TILES.get(tileCoordinate);
    }
	
	public  Map<Integer, Tile> getAllTiles() {
		return this.ALL_TILES;
	}
	
}