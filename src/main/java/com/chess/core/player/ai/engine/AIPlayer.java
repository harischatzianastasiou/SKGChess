package com.chess.core.player.ai.engine;

import com.chess.core.Alliance;
import  com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.player.ai.engine.strategy.MoveStrategy;

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