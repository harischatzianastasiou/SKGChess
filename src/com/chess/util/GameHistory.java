package com.chess.util;

import java.util.ArrayList;
import java.util.List;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
import com.chess.model.tiles.Tile;

public class GameHistory {

    private static GameHistory instance;
    private final List<IBoard> boards;
    private final List<Move> moveHistory;
    private int moveCount = 0; // for 50 move rule

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

    public int getHalfMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        moveCount++;
    }

    public void resetMoveCount() {
        moveCount = 0;
    }

    /**
     * Gets the count of how many times a specific board position has occurred in the game history.
     * @param board The board position to check
     * @return The number of times this position has occurred
     */
    public int getPositionCount(IBoard board) {
        int count = 1; // Start at 1 to count the current position
        for (IBoard historicBoard : boards) {
            if (isSamePosition(historicBoard, board)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if two board positions are identical (same pieces in same positions).
     * @param board1 First board to compare
     * @param board2 Second board to compare
     * @return true if the positions are identical
     */
    private boolean isSamePosition(IBoard board1, IBoard board2) {
        List<Tile> tiles1 = board1.getTiles();
        List<Tile> tiles2 = board2.getTiles();
        
        // Check each tile
        for (int i = 0; i < tiles1.size(); i++) {
            Piece piece1 = tiles1.get(i).getPiece();
            Piece piece2 = tiles2.get(i).getPiece();
            
            // If one has a piece and the other doesn't, positions are different
            if ((piece1 == null) != (piece2 == null)) {
                return false;
            }
            
            // If both have pieces, check if they're the same type and color
            if (piece1 != null && piece2 != null) {
                if (piece1.getPieceSymbol() != piece2.getPieceSymbol() ||
                    piece1.getPieceAlliance() != piece2.getPieceAlliance()) {
                    return false;
                }
            }
        }
        
        return true;
    }
}