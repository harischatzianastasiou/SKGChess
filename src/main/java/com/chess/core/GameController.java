package com.chess.core;

import com.chess.core.board.IBoard;
import com.chess.core.board.IChessBoard;
import com.chess.core.moves.Move;
import com.chess.core.player.CurrentPlayer;
import com.chess.core.player.Player;

public class GameController {
    private IBoard board;

    public GameController(IBoard board) {
        this.board = board;
    }

    public IBoard executeMove(IChessBoard chessBoard) {
        // Wait for player to select a move
        chessBoard.waitForPlayerMove();

        // Get selected tiles
        int sourceCoordinate = chessBoard.getSourceTile().getTileCoordinate();
        int targetCoordinate = chessBoard.getTargetTile().getTileCoordinate();

        // Find the move in the list of legal moves
        Move moveToExecute = null;
        for (Move move : board.getCurrentPlayer().getMoves()) {
            if (move.getSourceCoordinate() == sourceCoordinate 
                && move.getTargetCoordinate() == targetCoordinate) {
                moveToExecute = move;
                break;
            }
        }

        if (moveToExecute == null) {
            chessBoard.resetTileSelections();
            return null;
        }

        // Execute the move and update the board
        IBoard newBoard = moveToExecute.execute();
        this.board = newBoard;
        chessBoard.resetTileSelections();
        return newBoard;
    }

    public IBoard executeAIMove(Move move, IChessBoard chessBoard) {
        IBoard newBoard = move.execute();
        this.board = newBoard;
        chessBoard.updateBoard(newBoard);
        return newBoard;
    }

    public boolean isCheckmate() {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        return currentPlayer.isCheckmate();
    }

    public Player getCurrentPlayer() {
        return board.getCurrentPlayer();
    }

    public IBoard getBoard() {
        return board;
    }
} 