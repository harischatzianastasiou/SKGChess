package com.chess.model.moves;

import com.chess.model.board.Board;
import com.chess.model.moves.Move;
import com.chess.model.pieces.Piece;
public class MoveResult {

    private final Board postSimulationMoveBoard;
    private final Move simulationMove;
    private final boolean putsSelfInCheckmate;
    private final boolean isCheck;
    private final boolean isCastleKingSideLegal;
    private final boolean isCastleQueenSideLegal;

    private MoveResult(Move simulationMove, Board postSimulationMoveBoard) {
        this.postSimulationMoveBoard = postSimulationMoveBoard;
        this.simulationMove = simulationMove;
        this.putsSelfInCheckmate = putsSelfInCheckmate(postSimulationMoveBoard);
        this.isCheck = isCheck(postSimulationMoveBoard);
        this.isCastleKingSideLegal = canKingCastleKingSide(simulationMove, postSimulationMoveBoard);
        this.isCastleQueenSideLegal = canKingCastleQueenSide(simulationMove, postSimulationMoveBoard); 
    }

    public static MoveResult create(Move simulationMove, Board postSimulationMoveBoard) {
        return new MoveResult(simulationMove, postSimulationMoveBoard);
    }

    public Board getPostSimulationMoveBoard() {
        return postSimulationMoveBoard;
    }

    public Move getSimulationMove() {
        return simulationMove;
    }
    
    public boolean putsSelfInCheckmate() {
        return putsSelfInCheckmate;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public boolean isCastleKingSideLegal() {
        return isCastleKingSideLegal;
    }

    public boolean isCastleQueenSideLegal() {
        return isCastleQueenSideLegal;
    }

    private static boolean putsSelfInCheckmate(Board postSimulationMoveBoard) {//where current player is the player that made the move, so getOpponentPlayer() in the postSimulationMoveBoard.
        for (Move newCurrentPlayerPotentialMove : postSimulationMoveBoard.getCurrentPlayer().getPotentialLegalMoves()) {
            if (newCurrentPlayerPotentialMove.getTargetCoordinate() == postSimulationMoveBoard.getOpponentPlayer().getKing().getPieceCoordinate()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCheck(Board postSimulationMoveBoard) {
        // Get the piece that was just moved
        Piece movedPiece = simulationMove.getPieceToMove();
        
        // Iterate over the move maker's new legal moves, after the move is made ( move maker is the opponent in the postSimulationMoveBoard)
        for (Move newOpponentPotentialLegalMove : postSimulationMoveBoard.getOpponentPlayer().getPotentialLegalMoves()) {
            // Find the piece that was just moved
            // if (newOpponentPotentialLegalMove.getPieceToMove().equals(movedPiece)) {//equals doesnt work, fix later
                // Check if the piece that was just moved is attacking the new current player's king
                if (newOpponentPotentialLegalMove.getTargetCoordinate() == postSimulationMoveBoard.getCurrentPlayer().getKing().getPieceCoordinate()) {
                    return true;
                }
            // }
        }
        return false;
    }

    private boolean canKingCastleKingSide(Move simulationMove, Board postSimulationMoveBoard) {
        int newOpponentKingPositionBeforeCastling = simulationMove.getSourceCoordinate();// on previous board, current player's king position before castling
        int[] castlingPath = {newOpponentKingPositionBeforeCastling, newOpponentKingPositionBeforeCastling + 1, newOpponentKingPositionBeforeCastling + 2}; // include current king position
        
        for (Move newCurrentPlayerPotentialMove : postSimulationMoveBoard.getCurrentPlayer().getPotentialLegalMoves()) {
            int targetSquare = newCurrentPlayerPotentialMove.getTargetCoordinate();
            for (int pathSquare : castlingPath) {
                if (targetSquare == pathSquare) {
                    return false; // castling path or king's position is under attack
                }
            }
        }
        return true;
    }

    private boolean canKingCastleQueenSide(Move simulationMove, Board postSimulationMoveBoard) {
        int newOpponentKingPositionBeforeCastling = simulationMove.getSourceCoordinate();
        int[] castlingPath = {newOpponentKingPositionBeforeCastling, newOpponentKingPositionBeforeCastling - 1, newOpponentKingPositionBeforeCastling - 2}; // include current king position
        
        for (Move newCurrentPlayerPotentialMove : postSimulationMoveBoard.getCurrentPlayer().getPotentialLegalMoves()) {
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