package com.chess;

import com.chess.model.board.IBoard;
import com.chess.controller.GameController;
import com.chess.view.ChessBoardUI;

public class SKGChess {
    public static void main(String[] args) {
      IBoard board = IBoard.createStandardBoard();
        while(true) {
            GameController controller = new GameController(board);
            ChessBoardUI chessBoardUI = ChessBoardUI.getInstance(board);
            board = controller.executeMove(chessBoardUI);
        }
    }
} 