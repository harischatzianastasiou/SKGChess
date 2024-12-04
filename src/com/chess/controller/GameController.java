package com.chess.controller;

import java.util.Collection;

import com.chess.model.board.Board;
import com.chess.model.moves.Move;
import com.chess.util.GameHistory;
import com.chess.view.ChessBoardUI;

public class GameController {

    private boolean isCheckmate = false;
    private static GameController instance;

    private GameController() {}

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public Board executeMove(Board chessboard, ChessBoardUI chessBoardUI) {
        Collection<Move> validLegalMoves = chessboard.getCurrentPlayerValidMoves();

        for (Move validLegalMove : validLegalMoves) {
            if (validLegalMove.getSourceCoordinate() == chessBoardUI.getSourceTile().getTileCoordinate() &&
                validLegalMove.getTargetCoordinate() == chessBoardUI.getTargetTile().getTileCoordinate()) {

                    GameHistory.getInstance().addBoard(chessboard);
                    GameHistory.getInstance().addMove(validLegalMove);
    
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ///   With each move a new board is created to represent the new state of the tiles and the turn of the next players.     //           
                    ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
                    ///   Also Current and Opponent player have changed.                                                                      //
                    ///   The new current player gets the opposite color of the previous current player. Same applies for the opponent.       //
                    ///   Tiles, current player and opponent player are created with the board, and are immutable afterwards.                 //
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    chessboard = validLegalMove.execute();         

                if (chessboard.isCheckmate()) {
                    this.isCheckmate = true;
                }
                    break;
                }
        }
        return chessboard;
    }

    public boolean isCheckmate() {
        return this.isCheckmate;
    }

}