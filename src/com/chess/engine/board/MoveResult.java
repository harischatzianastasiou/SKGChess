package com.chess.engine.board;

public class MoveResult {

    private final Board simulatedBoard;
    private final Move simulationMove;
    private final boolean isCheckmate;
    private final boolean isCheck;
    private final boolean isCastleKingSideLegal;
    private final boolean isCastleQueenSideLegal;

    private MoveResult(Move move, Board simulatedBoard) {
        this.simulatedBoard = simulatedBoard;
        this.simulationMove = move;
        this.isCheckmate = putsSelfInCheckmate(simulatedBoard);
        this.isCheck = checksOpponent(simulatedBoard);
        this.isCastleKingSideLegal = canKingCastleKingSide(move, simulatedBoard);
        this.isCastleQueenSideLegal = canKingCastleQueenSide(move, simulatedBoard); 
    }

    public static MoveResult create(Move move, Board simulatedBoard) {
        return new MoveResult(move, simulatedBoard);
    }

    public Board getSimulatedBoard() {
        return simulatedBoard;
    }

    private static boolean putsSelfInCheckmate(Board simulatedBoard) {//where current player is the player that made the move, so getOpponentPlayer() in the simulatedBoard.
        for (Move newCurrentPlayerPotentialMove : simulatedBoard.getCurrentPlayer().getLegalMoves()) {
            if (newCurrentPlayerPotentialMove.getTargetCoordinate() == simulatedBoard.getOpponentPlayer().getKing().getPieceCoordinate()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checksOpponent(Board simulatedBoard) {
        for (Move opponentPotentialLegalMove : simulatedBoard.getOpponentPlayer().getLegalMoves()) {
            // Check if the opponent's potential move does not result in current player checkmate
                if (opponentPotentialLegalMove.getTargetCoordinate() == simulatedBoard.getCurrentPlayer().getKing().getPieceCoordinate()) {
                    
                    return true;
                }
        }
        return false;
    }
// name the players around the moves?? for example moveMaker is always the current player in current board, and the opponent in the newly created board.
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

    public Move getSimulationMove() {
        return simulationMove;
    }
    
    public boolean putsSelfInCheckmate() {
        return isCheckmate;
    }

    public boolean checksOpponent() {
        return isCheck;
    }

    public boolean isCastleKingSideLegal() {
        return isCastleKingSideLegal;
    }

    public boolean isCastleQueenSideLegal() {
        return isCastleQueenSideLegal;
    }
}