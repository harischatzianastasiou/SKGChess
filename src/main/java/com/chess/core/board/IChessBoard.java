package com.chess.core.board;

import com.chess.core.tiles.Tile;

/**
 * Common interface for both UI and headless chess boards
 */
public interface IChessBoard {
    void selectTile(int tileId);
    void waitForuserMove();
    Tile getSourceTile();
    Tile getTargetTile();
    void resetTileSelections();
    void updateBoard(IBoard board);
    IBoard getBoard();
} 