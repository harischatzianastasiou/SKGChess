package com.chess.engine.board;

import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;

public class MoveResult {

    public enum MoveStatus {
        LEGAL,
        CASTLE_ILLEGAL,
        CHECKMATE
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

    public Board getSimulatedBoard() {
        return simulatedBoard;
    }

    private static MoveStatus determineMoveStatus(Move move, Board simulatedBoard) {
        if (isKingInCheck(simulatedBoard)) {// If after executing the move, the king is in check, then it is checkmate.
            return MoveStatus.CHECKMATE;
        }
        if(move instanceof KingSideCastleMove){
            if(!canKingCastleKingSide(move, simulatedBoard)){
                return MoveStatus.CASTLE_ILLEGAL;
            }
        }
        if(move instanceof QueenSideCastleMove){
            if(!canKingCastleQueenSide(move, simulatedBoard)){
                return MoveStatus.CASTLE_ILLEGAL;
            }
        }
        return MoveStatus.LEGAL;
    }

    private static boolean isKingInCheck(Board simulatedBoard) {
        //preventing king from moving to a square that is under attack, and other pieces from moving to a square that does not prevent checkmate.
        for (Move newCurrentPlayerPotentialMove : simulatedBoard.getCurrentPlayer().getLegalMoves()) {
            if (newCurrentPlayerPotentialMove.getTargetCoordinate() == simulatedBoard.getOpponentPlayer().getKing().getPieceCoordinate()) {
                return true;
            }
        }
        return false;
    }

    private static boolean canKingCastleKingSide(Move move, Board simulatedBoard) {
        int newOpponentKingPositionBeforeCastling = move.getSourceCoordinate();// on previous board, current player's king position before castling
        int[] castlingPath = {newOpponentKingPositionBeforeCastling, newOpponentKingPositionBeforeCastling + 1, newOpponentKingPositionBeforeCastling + 2}; // include current king position
        
        for (Move newCurrentPlayerPotentialMove : simulatedBoard.getCurrentPlayer().getLegalMoves()) {
            int targetSquare = newCurrentPlayerPotentialMove.getTargetCoordinate();
            for (int pathSquare : castlingPath) {
                if (targetSquare == pathSquare) {
                    return false; // castling path or king's position is under attack
                }
            }
        }
        return true;
    }

    private static boolean canKingCastleQueenSide(Move move, Board simulatedBoard) {
        int newOpponentKingPositionBeforeCastling = move.getSourceCoordinate();
        int[] castlingPath = {newOpponentKingPositionBeforeCastling, newOpponentKingPositionBeforeCastling - 1, newOpponentKingPositionBeforeCastling - 2}; // include current king position
        
        for (Move newCurrentPlayerPotentialMove : simulatedBoard.getCurrentPlayer().getLegalMoves()) {
            int targetSquare = newCurrentPlayerPotentialMove.getTargetCoordinate();
            for (int pathSquare : castlingPath) {
                if (targetSquare == pathSquare) {
                    return false; // castling path or king's position is under attack
                }
            }
        }
        return true;
    }
}