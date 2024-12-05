package com.chess.model.board.validation;

import java.util.Collection;

import com.chess.model.moves.Move;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;


public class CastlingQueenSideValidation implements MoveValidationStrategy {
    
    @Override
    public boolean validate(Move move, Collection<Move> opponentPlayerMoves) {
        if (move instanceof QueenSideCastleMove) {
           return checkCastleQueenSide(move, opponentPlayerMoves);
        } 
        return true; // Placeholder
    }
    
    private boolean checkCastleQueenSide(Move simulationMove, Collection<Move> opponentPlayerMoves) {
        // int currentPlayerKingPositionBeforeCastling = simulationMove.getSourceCoordinate();
        // int[] castlingPath = {currentPlayerKingPositionBeforeCastling, currentPlayerKingPositionBeforeCastling - 1, currentPlayerKingPositionBeforeCastling - 2}; // include current king position
        
        // for (Move newCurrentPlayerPotentialMove :opponentPlayerMoves) {
        //     int targetSquare = newCurrentPlayerPotentialMove.getTargetCoordinate();
        //     for (int pathSquare : castlingPath) {
        //         if (targetSquare == pathSquare) {
        //             return false; // castling path or king's position is under attack
        //         }
        //     }
        // }
        return true;
    }
}

