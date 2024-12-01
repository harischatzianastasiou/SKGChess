package com.chess;

import com.chess.controller.GameController;
import com.chess.view.ChessBoardUI;

public class SKGChess {

    public static void main(String[] args) {	
		GameController gameController = GameController.getInstance();	
		ChessBoardUI chessboardUI = new ChessBoardUI(gameController);
    }
}