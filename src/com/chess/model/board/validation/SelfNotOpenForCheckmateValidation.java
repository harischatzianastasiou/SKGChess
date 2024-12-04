package com.chess.model.board.validation;

import com.chess.model.board.Board;
import com.chess.model.moves.Move;
import com.chess.model.player.Player;

public class SelfNotOpenForCheckmateValidation implements MoveValidationStrategy {

    @Override
    public boolean validate(Move move, Player opponentPlayer) {
        Board board = move.execute();
        for (Move opponentMove : board.getCurrentPlayer().getPotentialLegalMoves()) {
            if (opponentMove.getTargetCoordinate() == board.getOpponentPlayer().getKing().getPieceCoordinate()) {
                return false; // Move is invalid if it leaves the player in check
            }
        }
        return true;
    }
}