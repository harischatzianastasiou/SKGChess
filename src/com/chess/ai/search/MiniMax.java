package com.chess.ai.search;

import java.util.List;

import com.chess.ai.evaluation.BoardEvaluator;
import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.player.CurrentPlayer;

public class MiniMax {
    private final BoardEvaluator evaluator;
    private final int searchDepth;

    public MiniMax(BoardEvaluator evaluator, int searchDepth) {
        this.evaluator = evaluator;
        this.searchDepth = searchDepth;
    }

    public Move findBestMove(IBoard board) {
        List<Move> moves = board.getCurrentPlayer().getMoves().stream().toList();
        if (moves.isEmpty()) {
            throw new IllegalStateException("No moves available");
        }

        final long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        for (final Move move : moves) {
            currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                    min(move.execute(), this.searchDepth - 1) :
                    max(move.execute(), this.searchDepth - 1);

            if (board.getCurrentPlayer().getAlliance().isWhite() &&
                currentValue >= highestSeenValue) {
                highestSeenValue = currentValue;
                bestMove = move;
            } 
            else if (!board.getCurrentPlayer().getAlliance().isWhite() &&
                     currentValue <= lowestSeenValue) {
                lowestSeenValue = currentValue;
                bestMove = move;
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;
        
        return bestMove;
    }

    private int min(final IBoard board, final int depth) {
        if (depth == 0 || isEndGameScenario(board)) {
            return evaluator.evaluate(board, depth);
        }
        
        int lowestSeenValue = Integer.MAX_VALUE;
        for (final Move move : board.getCurrentPlayer().getMoves()) {
            final int currentValue = max(move.execute(), depth - 1);
            if (currentValue <= lowestSeenValue) {
                lowestSeenValue = currentValue;
            }
        }
        return lowestSeenValue;
    }

    private int max(final IBoard board, final int depth) {
        if (depth == 0 || isEndGameScenario(board)) {
            return evaluator.evaluate(board, depth);
        }
        
        int highestSeenValue = Integer.MIN_VALUE;
        for (final Move move : board.getCurrentPlayer().getMoves()) {
            final int currentValue = min(move.execute(), depth - 1);
            if (currentValue >= highestSeenValue) {
                highestSeenValue = currentValue;
            }
        }
        return highestSeenValue;
    }

    private static boolean isEndGameScenario(final IBoard board) {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();

        return currentPlayer.isCheckmate() ||currentPlayer.isStalemate();
    }
} 