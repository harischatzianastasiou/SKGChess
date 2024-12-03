package com.chess.model.moves;

import com.chess.model.pieces.King;
import com.chess.model.player.Player;
public class MoveValidation {

    private final boolean putsKingInCheck;
    private final boolean isCastleKingSideLegal;
    private final boolean isCastleQueenSideLegal;

    private MoveValidation(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        this.putsKingInCheck = putsKingInCheck(simulationMove, currentPlayer, opponentPlayer);
        this.isCastleKingSideLegal = canKingCastleKingSide(simulationMove, opponentPlayer);
        this.isCastleQueenSideLegal = canKingCastleQueenSide(simulationMove, opponentPlayer); 
    }

    public static MoveValidation create(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        return new MoveValidation(simulationMove, currentPlayer, opponentPlayer);
    }
    
    public boolean putsKingInCheck() {
        return putsKingInCheck;
    }

    public boolean isCastleKingSideLegal() {
        return isCastleKingSideLegal;
    }

    public boolean isCastleQueenSideLegal() {
        return isCastleQueenSideLegal;
    }

    private static boolean putsKingInCheck(Move simulationMove, Player currentPlayer, Player opponentPlayer) {//where current player is the player that made the move, so getOpponentPlayer() in the postSimulationMoveBoard.
        for (Move opponentPlayerPotentialMove : opponentPlayer.getPotentialLegalMoves()) {
                if (opponentPlayerPotentialMove.getTargetCoordinate() == simulationMove.getTargetCoordinate()) { // game controlller ischeck will be better
                    return true;
                }
        }
        return false;
    }

    public boolean isCurrentPlayerInCheck(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        for( Move move : this.getOpponentPlayer().getPotentialLegalMoves()){
             if(move.getTargetCoordinate() == this.getCurrentPlayer().getKing().getPieceCoordinate()){
                 return true;
             }
        }
        return false;
     }


    private boolean blocksCheck(Move simulationMove, Player currentPlayer, Player opponentPlayer) {
        int kingPosition = currentPlayer.getKing().getPieceCoordinate();
        
                // Calculate the path of attack
                int[] attackPath = calculateAttackPath(opponentPlayerPotentialMove.getSourceCoordinate(), kingPosition);
                
                // Check if the simulationMove blocks this path
                for (int pathSquare : attackPath) {
                    if (simulationMove.getTargetCoordinate() == pathSquare) {
                        return true; // The move blocks the path of attack
                    }
                }
            }
        }
        return false;
    }

    private int[] calculateAttackPath(int source, int target) {
        // Calculate the path from source to target
        // This is a simplified example; you need to adjust it based on your board representation
        // and the type of piece (e.g., rook, bishop, queen) that is attacking.
        
        // Example for a vertical attack (same file):
        if (source % 8 == target % 8) {
            int step = (target > source) ? 8 : -8;
            int[] path = new int[Math.abs(target - source) / 8];
            for (int i = 0, pos = source + step; pos != target; pos += step, i++) {
                path[i] = pos;
            }
            return path;
        }
        
        // Add logic for other types of attacks (horizontal, diagonal, etc.)
        
        return new int[0]; // Return an empty path if no valid path is found
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