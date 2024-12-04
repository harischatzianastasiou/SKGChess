package com.chess.model.board.validation;

import com.chess.model.moves.Move;
import com.chess.model.player.Player;

public interface MoveValidationStrategy {
    public abstract boolean validate(Move move, Player opponentPlayer);
}