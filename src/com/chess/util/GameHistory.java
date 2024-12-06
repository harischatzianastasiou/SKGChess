package com.chess.util;

import java.util.ArrayList;
import java.util.List;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;

public class GameHistory {

    private static GameHistory instance;
    private final List<IBoard> boards;
    private final List<Move> moveHistory;

    private GameHistory() {
        this.boards = new ArrayList<>();
        this.moveHistory = new ArrayList<>();
    }
    
    // Singleton pattern implementation to ensure only one instance of BoardHistory is created.
    public static GameHistory getInstance() {
        if (instance == null) {
            instance = new GameHistory();
        }
        return instance;
    }
    
    public void addMove(Move move) {
    	this.moveHistory.add(move);
    }

    public void addBoard(IBoard board) {
        this.boards.add(board);
    }

    public List<IBoard> getBoards() {
        return new ArrayList<>(this.boards);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(this.moveHistory);
    }

    public IBoard getLastBoard() {
        if (!boards.isEmpty()) {
            return boards.get(boards.size() - 1);
        }
        return null;
    }
    
    public Move getLastMove() {
        if (!moveHistory.isEmpty()) {
            return moveHistory.get(moveHistory.size() - 1);
        }
        return null;
    }
}