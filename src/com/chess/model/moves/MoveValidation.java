package com.chess.model.moves;

import com.chess.model.pieces.King;
import com.chess.model.player.Player;
public class MoveValidation {

    private final boolean putsSelfInCheckmate;
    private final boolean isCastleKingSideLegal;
    private final boolean isCastleQueenSideLegal;

    private MoveValidation(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        this.putsSelfInCheckmate = putsSelfInCheckmate(simulationMove, currentPlayer, opponentPlayer);
        this.isCastleKingSideLegal = canKingCastleKingSide(simulationMove, opponentPlayer);
        this.isCastleQueenSideLegal = canKingCastleQueenSide(simulationMove, opponentPlayer); 
    }

    public static MoveValidation create(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        return new MoveValidation(simulationMove, currentPlayer, opponentPlayer);
    }
    
    public boolean putsSelfInCheckmate() {
        return putsSelfInCheckmate;
    }

    public boolean isCastleKingSideLegal() {
        return isCastleKingSideLegal;
    }

    public boolean isCastleQueenSideLegal() {
        return isCastleQueenSideLegal;
    }

    private static boolean putsSelfInCheckmate(Move simulationMove, Player currentPlayer, Player opponentPlayer) {//where current player is the player that made the move, so getOpponentPlayer() in the postSimulationMoveBoard.
        for (Move opponentPlayerPotentialMove : opponentPlayer.getPotentialLegalMoves()) {
            if(!(simulationMove.getPieceToMove() instanceof King)){
                if (opponentPlayerPotentialMove.getTargetCoordinate() == currentPlayer.getKing().getPieceCoordinate()) { // game controlller ischeck will be better
                    return true;
                }
            }
            else{
                if (opponentPlayerPotentialMove.getTargetCoordinate() == simulationMove.getTargetCoordinate()) { // game controlller ischeck will be better
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canKingCastleKingSide(Move simulationMove, Player opponentPlayer) {
        int currentPlayerKingPositionBeforeCastling = simulationMove.getSourceCoordinate();// on previous board, current player's king position before castling
        int[] castlingPath = {currentPlayerKingPositionBeforeCastling, currentPlayerKingPositionBeforeCastling + 1, currentPlayerKingPositionBeforeCastling + 2}; // include current king position
        
        for (Move opponentPlayerPotentialMove : opponentPlayer.getPotentialLegalMoves()) {
            int targetSquare = opponentPlayerPotentialMove.getTargetCoordinate();
            for (int pathSquare : castlingPath) {
                if (targetSquare == pathSquare) {
                    return false; // castling path or king's position is under attack
                }
            }
        }
        return true;
    }

    private boolean canKingCastleQueenSide(Move simulationMove, Player opponentPlayer) {
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
        return true;
    }
}