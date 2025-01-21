package com.chess.ai.strategy;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;

public interface MoveStrategy {
    /**
     * Gets the best move for the current position according to this strategy.
     * @param board The current board position
     * @return The best move found
     * @throws IllegalStateException if no legal moves are available
     */
    Move getBestMove(IBoard board);

    /**
     * Gets the name of this strategy.
     * @return The strategy name
     */
    String getName();
} 