package com.chess.ai;

import com.chess.ai.strategy.MoveStrategy;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;

public class AIPlayer {
    private final MoveStrategy strategy;
    private final Alliance alliance;

    public AIPlayer(MoveStrategy strategy, Alliance alliance) {
        this.strategy = strategy;
        this.alliance = alliance;
    }

    public Move makeMove(IBoard board) {
        if (board.getCurrentPlayer().getAlliance() != alliance) {
            throw new IllegalStateException("Not this player's turn");
        }
        return strategy.getBestMove(board);
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public String getStrategyName() {
        return strategy.getName();
    }
} 