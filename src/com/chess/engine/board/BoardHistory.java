package com.chess.engine.board;

import java.util.ArrayList;
import java.util.List;

import com.chess.engine.pieces.Piece;

public class BoardHistory {

    private static BoardHistory instance;
    private final List<Board> boardStates;

    private BoardHistory() {
        this.boardStates = new ArrayList<>();
    }
    
    // Singleton pattern implementation to ensure only one instance of BoardHistory is created.
    public static BoardHistory getInstance() {
        if (instance == null) {
            instance = new BoardHistory();
        }
        return instance;
    }

    public void addBoardState(Board board) {
        this.boardStates.add(board);
        calculateLastMove();
    }

    public List<Board> getBoardStates() {
        return new ArrayList<>(this.boardStates);
    }

    public Board getLastBoardState() {
        if (!boardStates.isEmpty()) {
            return boardStates.get(boardStates.size() - 1);
        }
        return null;
    }

    public void calculateLastMove() {
        if (boardStates.size() < 2) {
            return; // Not enough states to calculate a move
        }
        Board lastBoard = boardStates.get(boardStates.size() - 2);
        Board currentBoard = boardStates.get(boardStates.size() - 1);
        List<Tile> lastTiles = lastBoard.getTiles();
        List<Tile> currentTiles = currentBoard.getTiles();

        for (int i = 0; i < currentTiles.size(); i++) {
            Tile currentTile = currentTiles.get(i);
            Tile lastTile = lastTiles.get(i);
            if (!currentTile.equals(lastTile)) {
                Piece movedPiece = currentTile.getPiece();
                if (movedPiece != null) {
                    System.out.println("Piece moved: " + movedPiece + " from " + lastTile + " to " + currentTile);
                }
            }
        }
    }
}