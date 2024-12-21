package com.chess.controller;

import java.util.Collection;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.player.CurrentPlayer;
import com.chess.util.GameHistory;
import com.chess.util.SoundPlayer;
import com.chess.view.ChessBoardUI;

public class GameController {

    private boolean isGameOver = false;
    private IBoard board;

    public GameController(IBoard board) {
        this.board = board;
    }

    public IBoard executeMove(ChessBoardUI chessBoardUI) {

        chessBoardUI.waitForPlayerMove();
        Collection<Move> currentPlayerMoves = board.getCurrentPlayer().getMoves();

        for (Move currentPlayerMove : currentPlayerMoves) {
            if (currentPlayerMove.getSourceCoordinate() == chessBoardUI.getSourceTile().getTileCoordinate() &&
                currentPlayerMove.getTargetCoordinate() == chessBoardUI.getTargetTile().getTileCoordinate()) {
                GameHistory.getInstance().addBoard(board);
                GameHistory.getInstance().addMove(currentPlayerMove);
                       
                // Play appropriate sound based on move type
                if(currentPlayerMove instanceof CapturingMove) {
                    SoundPlayer.playCaptureSound();
                } else {
                    SoundPlayer.playMoveSound();
                }
    
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///   With each move a new board is created to represent the new state of the tiles and the turn of the next players.     //           
                ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
                ///   Also Current and Opponent player have changed.                                                                      //
                ///   The new current player gets the opposite color of the previous current player. Same applies for the opponent.       //
                ///   Tiles, current player and opponent player are created with the board, and are immutable afterwards.                 //
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                board = currentPlayerMove.execute(); 
                
                CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();

                if (currentPlayer.isCheckmate()) {
                    SoundPlayer.playCheckmateSound();
                }else if(currentPlayer.isInCheck()) {
                    System.out.println("Checking");
                    SoundPlayer.playCheckSound();
                }

                if (currentPlayer.isCheckmate()) {
                    this.isGameOver = true;
                }
                    break;
                }
        }
        return board;
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

}