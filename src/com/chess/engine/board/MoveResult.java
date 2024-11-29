package com.chess.engine.board;

import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import com.chess.engine.pieces.King;
import com.chess.engine.player.Player;

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
        if (isMoveLeavingKingOpenForCheckmate(simulatedBoard)) {
            return MoveStatus.CHECKMATE;
        }
        if(move instanceof KingSideCastleMove){
            if(!isKingSideCastleValid(move, simulatedBoard)){
                return MoveStatus.ILLEGAL;
            }
        }
        if(move instanceof QueenSideCastleMove){
            if(!isQueenSideCastleValid(move, simulatedBoard)){
                return MoveStatus.ILLEGAL;
            }
        }
        return MoveStatus.LEGAL;
    }

    private static boolean isMoveLeavingKingOpenForCheckmate(Board simulatedBoard) {
        //preventing king from moving to a square that is under attack, and other pieces from moving to a square that does not prevent checkmate.
        Player opponent = simulatedBoard.getCurrentPlayer();
        King currentPlayerKing = simulatedBoard.getOpponentPlayer().getKing();
        for (Move opponentMove : opponent.getLegalMoves()) {
            if (opponentMove.getTargetCoordinate() == currentPlayerKing.getPieceCoordinate()) {
                System.out.println("Found attacking move: " + opponentMove.getPieceToMove() + 
                                 " from " + opponentMove.getSourceCoordinate() + 
                                 " to " + opponentMove.getTargetCoordinate());
                System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\");
                System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\");
                return true;
            }
        }
        return false;
    }

    private static boolean isKingSideCastleValid(Move move, Board simulatedBoard) {
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

    private static boolean isQueenSideCastleValid(Move move, Board simulatedBoard) {
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
}