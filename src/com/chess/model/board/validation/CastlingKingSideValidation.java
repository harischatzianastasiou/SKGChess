package com.chess.model.board.validation;

import com.chess.model.moves.Move;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.player.Player;

public class CastlingKingSideValidation implements MoveValidationStrategy {

    @Override
    public boolean validate(Move move, Player opponentPlayer) {
        if (move instanceof KingSideCastleMove) {
           return checkCastleKingSide(move, opponentPlayer);
        } 
        return true; // Placeholder
    }

    private boolean checkCastleKingSide(Move simulationMove, Player opponentPlayer) {
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
} 