package com.chess.engine.board;

import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import com.chess.engine.pieces.King;
import com.chess.engine.player.Player;

public class MoveResult {
    public enum MoveStatus {
        ILLEGAL,
        LEGAL,
        CHECK
    }

    private static final MoveResult DEFAULT_INSTANCE = new MoveResult();

    private final Board simulatedBoard;
    private final MoveStatus moveStatus;

    private MoveResult(Move move, Board simulatedBoard) {
        this.simulatedBoard = simulatedBoard;
        this.moveStatus = determineMoveStatus(move, simulatedBoard);
    }

    private MoveResult() { // 1st move of each player
        this.simulatedBoard = null;
        this.moveStatus = MoveStatus.LEGAL;
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

    private static MoveStatus determineMoveStatus(Move move, Board simulatedBoard) {
        if (checkIfMovePutsKingIntoCheck(simulatedBoard)) {
            return MoveStatus.ILLEGAL;
        }
        if(move instanceof KingSideCastleMove){
            if(!checkIfKingSideCastleValid(move, simulatedBoard)){
                return MoveStatus.ILLEGAL;
            }
        }
        if(move instanceof QueenSideCastleMove){
            if(!checkIfQueenSideCastleValid(move, simulatedBoard)){
                return MoveStatus.ILLEGAL;
            }
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
        int kingPosition = move.getSourceCoordinate();
        int[] castlingPath = {kingPosition, kingPosition + 1, kingPosition + 2}; // include current king position
        
        for (Move opponentMove : opponent.getLegalMoves()) {
            int targetSquare = opponentMove.getTargetCoordinate();
            for (int pathSquare : castlingPath) {
                if (targetSquare == pathSquare) {
                    return false; // castling path or king's position is under attack
                }
            }
        }
        return true;
    }

    private static boolean checkIfQueenSideCastleValid(Move move, Board simulatedBoard) {
        Player opponent = simulatedBoard.getCurrentPlayer();
        int kingPosition = move.getSourceCoordinate();
        int[] castlingPath = {kingPosition, kingPosition - 1, kingPosition - 2}; // include current king position
        
        for (Move opponentMove : opponent.getLegalMoves()) {
            int targetSquare = opponentMove.getTargetCoordinate();
            for (int pathSquare : castlingPath) {
                if (targetSquare == pathSquare) {
                    return false; // castling path or king's position is under attack
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