package com.chess.core.player.ai.engine.evaluation;

import  com.chess.core.board.IBoard;


public interface BoardEvaluator {
    /**
     * Evaluates a board position and returns a score.
     * Positive scores favor white, negative scores favor black.
     * @param board The board to evaluate
     * @return The evaluation score
     */
    
    int evaluate(IBoard board, final int depth);
} 