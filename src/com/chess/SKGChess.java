package com.chess;

import com.chess.model.board.IBoard;
import com.chess.controller.GameController;
import com.chess.view.ChessBoardUI;

public class SKGChess {
    public static void main(String[] args) {
        IBoard board = IBoard.createStandardBoard();
        while (true) {
            try {
                GameController controller = new GameController(board);
                ChessBoardUI chessBoardUI = ChessBoardUI.getInstance(board);
                board = controller.executeMove(chessBoardUI);
                
                // // Add a condition to break the loop (e.g., check for game over)
                // if (controller.isGameOver()) {
                //     break; // Exit the loop if the game is over
                // }
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions gracefully
            }
        }
    }
}