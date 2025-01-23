package com.chess.web.game;

import com.chess.core.board.IBoard;
import com.chess.core.board.IChessBoard;
import com.chess.core.pieces.Piece;
import com.chess.core.tiles.Tile;

/**
 * A headless version of ChessBoardUI for server-side use
 */
public class HeadlessChessBoard implements IChessBoard {
    private IBoard board;
    private Tile sourceTile;
    private Tile targetTile;
    private Piece sourceSelectedPiece;
    private Piece targetSelectedPiece;
    private volatile boolean moveSelected = false;

    public HeadlessChessBoard(IBoard board) {
        this.board = board;
    }

    public void selectTile(int tileId) {
        if (sourceTile == null) {
            sourceTile = board.getTile(tileId);
            sourceSelectedPiece = sourceTile.getPiece();
        } else {
            targetTile = board.getTile(tileId);
            targetSelectedPiece = targetTile.getPiece();
            moveSelected = true;
        }
    }

    public void waitForPlayerMove() {
        // No need to actually wait in headless mode
        // Move is selected immediately after setting source and target tiles
    }

    public Tile getSourceTile() {
        return sourceTile;
    }

    public Tile getTargetTile() {
        return targetTile;
    }

    public void resetTileSelections() {
        this.sourceTile = null;
        this.targetTile = null;
        this.sourceSelectedPiece = null;
        this.targetSelectedPiece = null;
        this.moveSelected = false;
    }

    public void updateBoard(IBoard board) {
        this.board = board;
    }

    public IBoard getBoard() {
        return board;
    }
} 