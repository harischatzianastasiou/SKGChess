package com.chess.engine.board;

import java.util.ArrayList;
import java.util.List;

import com.chess.engine.pieces.Piece;

public class GameHistory {

    private static GameHistory instance;
    private final List<Board> boardStates;
    private final List<Move> moveHistory;

    private GameHistory() {
        this.boardStates = new ArrayList<>();
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

    public void addBoardState(Board board) {
        this.boardStates.add(board);
    }

    public List<Board> getBoardStates() {
        return new ArrayList<>(this.boardStates);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(this.moveHistory);
    }

    public Board getLastBoardState() {
        if (!boardStates.isEmpty()) {
            return boardStates.get(boardStates.size() - 1);
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