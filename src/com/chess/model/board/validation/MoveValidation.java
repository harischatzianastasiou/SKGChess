package com.chess.model.board.validation;

import java.util.Collection;
import java.util.List;

import com.chess.model.moves.Move;

public class MoveValidation {

    private final List<MoveValidationStrategy> strategies;

    public MoveValidation(List<MoveValidationStrategy> strategies) {
        this.strategies = strategies;
    }

    public boolean validate(Move move, Collection<Move> opponentPlayerMoves) {
        for (MoveValidationStrategy strategy : strategies) {
            if (!strategy.validate(move, opponentPlayerMoves)) {
                return false;
            }
        }
        return true;
    }
}