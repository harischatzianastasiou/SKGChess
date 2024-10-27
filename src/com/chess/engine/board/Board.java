package com.chess.engine.board;

import com.chess.engine.board.BoardUtils;

public class Board {
	
	public Tile getTile(final int tileCoordinate) {
        return BoardUtils.EMPTY_TILES_CACHE.get(tileCoordinate);
    }

}
