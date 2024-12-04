package com.chess.model.board.validation;

import java.util.Collection;

import com.chess.model.moves.Move;

public class SelfNotOpenForCheckmateValidation implements MoveValidationStrategy {

    @Override
    public boolean validate(Move move, Collection<Move> opponentPlayerMoves) {
        // Board board = move.execute();
        // for (Move opponentMove : opponentPlayerMoves) {
        //     if (opponentMove.getTargetCoordinate() == board.getOpponentPlayer().getKing().getPieceCoordinate()) {
        //         return false; // Move is invalid if it leaves the player in check
        //     }
        // }
        return true;
    }
}