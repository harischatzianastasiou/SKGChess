package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.google.common.collect.ImmutableMap;

public class Board {
	
	public static Map<Integer, Tile> ALL_TILES = new HashMap<> (BoardUtils.createAllPossibleEmptyTiles());

	public Tile getTile(final int tileCoordinate) {
        return ALL_TILES.get(tileCoordinate);
    }
}