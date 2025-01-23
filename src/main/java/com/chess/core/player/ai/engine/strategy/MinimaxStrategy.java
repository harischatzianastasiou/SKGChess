package com.chess.core.player.ai.engine.strategy;

import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import  com.chess.core.player.ai.engine.evaluation.BoardEvaluator;
import  com.chess.core.player.ai.engine.search.MiniMax;

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