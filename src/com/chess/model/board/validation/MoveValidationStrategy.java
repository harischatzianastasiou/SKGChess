package com.chess.model.board.validation;

import java.util.Collection;

import com.chess.model.moves.Move;

public interface MoveValidationStrategy {
    public abstract boolean validate(Move move, Collection<Move> opponentPlayerMoves);
}