package com.chess.model.board;

import java.util.List;

import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public interface IBoard {
    List<Tile> getTiles();
    Tile getTile(int tileCoordinate);
    Player getCurrentPlayer();
    Player getOpponentPlayer();
    static IBoard createStandardBoard() {
        return Board.createStandardBoard();
    }
} 