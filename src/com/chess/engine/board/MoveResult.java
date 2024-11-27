package com.chess.engine.board;

import com.chess.engine.pieces.Piece;
import com.chess.engine.board.Move.BlockingKingSideCastleMove;
import com.chess.engine.board.Move.BlockingQueenSideCastleMove;
import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import com.chess.engine.pieces.King;
import com.chess.engine.player.Player;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

public class MoveResult {
    public enum MoveStatus {
        ILLEGAL,
        LEGAL,
        CHECK
    }

    private static final MoveResult DEFAULT_INSTANCE = new MoveResult();

    private final Board simulatedBoard;
    private final MoveStatus moveStatus;
    private final Collection<Move> opponentLegalMoves;

    private MoveResult(Move move, Board simulatedBoard) {
        this.simulatedBoard = simulatedBoard;
        this.moveStatus = determineMoveStatus(move, simulatedBoard);
        this.opponentLegalMoves = move.undo().getCurrentPlayer().getLegalMoves();
    }

    private MoveResult() { // 1st move of each player
        this.simulatedBoard = null;
        this.moveStatus = MoveStatus.LEGAL;
        this.opponentLegalMoves = null;
    }

    public static MoveResult getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static MoveResult create(Move move, Board simulatedBoard) {
        return new MoveResult(move, simulatedBoard);
    }

    public MoveStatus getMoveStatus() {
        return moveStatus;
    }
    
	public Collection<Move> getOpponentLegalMoves() {
	    if (simulatedBoard == null || simulatedBoard.getOpponentPlayer() == null) {
	        return ImmutableList.of(); // Return an empty list instead of null
	    }
	    return simulatedBoard.getOpponentPlayer().getLegalMoves();
	}

    private static MoveStatus determineMoveStatus(Move move, Board simulatedBoard) {
        if (checkIfMovePutsKingIntoCheck(simulatedBoard)) {
            return MoveStatus.ILLEGAL;
        }
        if (!checkIfKingSideCastleValid(move, simulatedBoard) || !checkIfQueenSideCastleValid(move, simulatedBoard)) {
            return MoveStatus.ILLEGAL;
        }
        if (isOpponentKingInCheck(move, simulatedBoard)) {
            return MoveStatus.CHECK;
        }
        return MoveStatus.LEGAL;
    }

    private static boolean checkIfMovePutsKingIntoCheck(Board simulatedBoard) {
        Player opponent = simulatedBoard.getCurrentPlayer();
        King king = simulatedBoard.getOpponentPlayer().getKing();

        for (Move opponentMove : opponent.getLegalMoves()) {
            if (opponentMove.getTargetCoordinate() == king.getPieceCoordinate()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfKingSideCastleValid(Move move, Board simulatedBoard) {
        Player opponent = simulatedBoard.getCurrentPlayer();

        if (move instanceof KingSideCastleMove) {
            for (Move opponentMove : opponent.getLegalMoves()) {
                if (opponentMove instanceof BlockingKingSideCastleMove) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIfQueenSideCastleValid(Move move, Board simulatedBoard) {
        Player opponent = simulatedBoard.getCurrentPlayer();

        if (move instanceof QueenSideCastleMove) {
            for (Move opponentMove : opponent.getLegalMoves()) {
                if (opponentMove instanceof BlockingQueenSideCastleMove) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isOpponentKingInCheck(Move move,Board simulatedBoard) {
        King king = simulatedBoard.getCurrentPlayer().getKing();
        if (move.getTargetCoordinate() == king.getPieceCoordinate()) {
            return true;
        }
        return false;
    }
}