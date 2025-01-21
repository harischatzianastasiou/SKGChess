package com.chess.ai.strategy;

import com.chess.ai.evaluation.BoardEvaluator;
import com.chess.ai.search.MiniMax;
import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;

public class MinimaxStrategy implements MoveStrategy {
    private final MiniMax minimax;
    private final int depth;

    public MinimaxStrategy(BoardEvaluator evaluator, int depth) {
        this.minimax = new MiniMax(evaluator, depth);
        this.depth = depth;
    }

    @Override
    public Move getBestMove(IBoard board) {
        return minimax.findBestMove(board);
    }

    @Override
    public String getName() {
        return String.format("Minimax (depth %d)", depth);
    }
} 