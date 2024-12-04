package com.chess.model.board.validation;

import com.chess.model.moves.Move;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.player.Player;

public class CastlingQueenSideValidation implements MoveValidationStrategy {
    
    @Override
    public boolean validate(Move move, Player opponentPlayer) {
        if (move instanceof QueenSideCastleMove) {
           return checkCastleQueenSide(move, opponentPlayer);
        } 
        return true; // Placeholder
    }
    
    private boolean checkCastleQueenSide(Move simulationMove, Player opponentPlayer) {
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

