package com.chess.model.moves;

import com.chess.model.board.Board;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.pieces.King;
import com.chess.model.player.Player;
public class MoveValidation {

    private final boolean selfInCheck;
    private final boolean isCastleKingSideLegal;
    private final boolean isCastleQueenSideLegal;

    private MoveValidation(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        this.selfInCheck = selfInCheck(simulationMove, currentPlayer, opponentPlayer);
        this.isCastleKingSideLegal = canKingCastleKingSide(simulationMove, opponentPlayer);
        this.isCastleQueenSideLegal = canKingCastleQueenSide(simulationMove, opponentPlayer); 
    }

    public static MoveValidation create(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        return new MoveValidation(simulationMove, currentPlayer, opponentPlayer);
    }
    
    public boolean selfInCheck() {
        return selfInCheck;
    }

    public boolean isCastleKingSideLegal() {
        return isCastleKingSideLegal;
    }

    public boolean isCastleQueenSideLegal() {
        return isCastleQueenSideLegal;
    }

    private static boolean selfInCheck(Move simulationMove, Player currentPlayer, Player opponentPlayer) {//where current player is the player that made the move, so getOpponentPlayer() in the postSimulationMoveBoard.
        Board board = simulationMove.execute();
        for (Move currentPlayerPotentialMove : currentPlayer.getPotentialLegalMoves()) {
            if(!(simulationMove.getPieceToMove() instanceof King)){
                if (currentPlayerPotentialMove.getTargetCoordinate() == opponentPlayer.getKing().getPieceCoordinate()) { // game controlller ischeck will be better
                    return true;
                }
            }
            else{
                if (currentPlayerPotentialMove.getTargetCoordinate() == simulationMove.getTargetCoordinate()) { // game controlller ischeck will be better
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canKingCastleKingSide(Move simulationMove, Player opponentPlayer) {
        boolean isLegal = false;
        if(simulationMove instanceof KingSideCastleMove){
            int currentPlayerKingPositionBeforeCastling = simulationMove.getSourceCoordinate();// on previous board, current player's king position before castling
            int[] castlingPath = {currentPlayerKingPositionBeforeCastling, currentPlayerKingPositionBeforeCastling + 1, currentPlayerKingPositionBeforeCastling + 2}; // include current king position
            
            for (Move opponentPlayerPotentialMove : opponentPlayer.getPotentialLegalMoves()) {
                int targetSquare = opponentPlayerPotentialMove.getTargetCoordinate();
                for (int pathSquare : castlingPath) {
                    if (targetSquare == pathSquare) {
                        return false; // castling path or king's position is under attack
                    }
                }
                isLegal = true;
            }
        }
        return isLegal;
    }

    private boolean canKingCastleQueenSide(Move simulationMove, Player opponentPlayer) {
        boolean isLegal = false;
        if(simulationMove instanceof KingSideCastleMove){
            int currentPlayerKingPositionBeforeCastling = simulationMove.getSourceCoordinate();
            int[] castlingPath = {currentPlayerKingPositionBeforeCastling, currentPlayerKingPositionBeforeCastling - 1, currentPlayerKingPositionBeforeCastling - 2}; // include current king position
            
            for (Move newCurrentPlayerPotentialMove :opponentPlayer.getPotentialLegalMoves()) {
                int targetSquare = newCurrentPlayerPotentialMove.getTargetCoordinate();
                for (int pathSquare : castlingPath) {
                    if (targetSquare == pathSquare) {
                        return false; // castling path or king's position is under attack
                    }
                }
            }
            isLegal = true;
        }
        return isLegal;
    }


}