package com.chess.controller;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.model.board.Board;
import com.chess.model.moves.Move;
import com.chess.model.moves.MoveResult;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Knight;
import com.chess.model.pieces.Pawn;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Queen;
import com.chess.model.pieces.Rook;
import com.chess.model.tiles.Tile;
import com.chess.test.BishopTest;
import com.chess.test.KingTest;
import com.chess.test.KnightTest;
import com.chess.test.PawnTest;
import com.chess.test.QueenTest;
import com.chess.test.RookTest;
import com.chess.util.GameHistory;
import com.chess.view.ChessBoardUI;

public class MoveValidator {

    private Board chessboard;
    private ChessBoardUI chessBoardUI;
    private boolean isCheckmate;

    public MoveValidator(Board chessboard,ChessBoardUI chessBoardUI) {
        this.chessboard = chessboard;
        this.chessBoardUI = chessBoardUI;
        this.isCheckmate = false;
    }

    public Board validateMove() {
        boolean isCurrentPlayerInCheck = false;
        boolean isCurrentPlayerInCheckmate = false;
        Collection<Move> potentialLegalMoves = new ArrayList<>(chessboard.getCurrentPlayer().getPotentialLegalMoves());
        Collection<Move> validLegalMoves = potentialLegalMoves;
        Collection<MoveResult> checkingMoveResults = new ArrayList<>();
        MoveResult simulationMoveResult = null;
        for(Move simulationMove : chessboard.getCurrentPlayer().getPotentialLegalMoves()) {  
            simulationMoveResult = simulationMove.simulate();
            
            if(simulationMove instanceof KingSideCastleMove){
                if(!simulationMoveResult.isCastleKingSideLegal()){
                    potentialLegalMoves.remove(simulationMove);
                }
            }else if(simulationMove instanceof QueenSideCastleMove){
                if(!simulationMoveResult.isCastleQueenSideLegal()){
                    potentialLegalMoves.remove(simulationMove);
                }
            }
            
            if(simulationMoveResult.putsSelfInCheckmate()) { // Simulate all potential moves of current player to find out which moves cannot be played due to opponent blocking them ( Moves like moving into check, moving a pinned piece etc are illegal and are checked here. Until this point current player only knew where his pieces could go, without taking account opponent's moves).
                potentialLegalMoves.remove(simulationMove); // Remove illegal moves from current player's potential legal moves list.
            }
            
            if(simulationMoveResult.isCheck()){
                checkingMoveResults.add(simulationMoveResult);
            }
            
        }

        Tile sourceTile = chessBoardUI.getSourceTile();
        Tile targetTile = chessBoardUI.getTargetTile();
        Piece selectedPiece = sourceTile.getPiece();

        if(selectedPiece.getPieceAlliance() == chessboard.getCurrentPlayer().getAlliance()) {
            if(selectedPiece instanceof Rook) {
            System.out.println("\nTesting Rook Moves:");
            RookTest.testRookMovesWithStandardBoard(validLegalMoves,selectedPiece.getPieceCoordinate());
            }else if(selectedPiece instanceof Knight) {
            System.out.println("\nTesting Knight Moves:");
            KnightTest.testKnightMovesWithStandardBoard(validLegalMoves,selectedPiece.getPieceCoordinate());
            }else if(selectedPiece instanceof Bishop) {
            System.out.println("\nTesting Bishop Moves:");
            BishopTest.testBishopMovesWithStandardBoard(validLegalMoves,selectedPiece.getPieceCoordinate());
            }else if(selectedPiece instanceof Queen) {
            System.out.println("\nTesting Queen Moves:");
            QueenTest.testQueenMovesWithStandardBoard(validLegalMoves,selectedPiece.getPieceCoordinate());
            }else if(selectedPiece instanceof King) {
            System.out.println("\nTesting King Moves:");
            KingTest.testKingMovesWithStandardBoard(validLegalMoves,selectedPiece.getPieceCoordinate());
            }else if(selectedPiece instanceof Pawn) {
            System.out.println("\nTesting Pawn Moves:");
            PawnTest.testPawnMovesWithStandardBoard(validLegalMoves,selectedPiece.getPieceCoordinate());
            }
        }
        for(Move validLegalMove : validLegalMoves) {  
            if(validLegalMove.getSourceCoordinate() == sourceTile.getTileCoordinate() && validLegalMove.getTargetCoordinate() == targetTile.getTileCoordinate()) {
            
                GameHistory.getInstance().addBoard(chessboard);
                GameHistory.getInstance().addMove(validLegalMove);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///   With each move a new board is created to represent the new state of the tiles and the turn of the next players.     //           
        ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
        ///   Also Current and Opponent player have changed.                                                                      //
        ///   The new current player gets the opposite color of the previous current player. Same applies for the opponent.       //
        ///   Tiles, current player and opponent player are created with the board, and are immutable afterwards.                 //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
                System.out.println(checkingMoveResults.toString());
                chessboard = validLegalMove.execute(); 
                if(checkingMoveResults.stream().anyMatch(moveResult -> moveResult.getSimulationMove().getSourceCoordinate() == validLegalMove.getSourceCoordinate() && 
                                                                    moveResult.getSimulationMove().getTargetCoordinate() == validLegalMove.getTargetCoordinate())) {
                    this.isCheckmate = true; // Assume checkmate until proven otherwise
                    for(Move currentPlayerPotentialMove  : chessboard.getCurrentPlayer().getPotentialLegalMoves()) { 
                        MoveResult currentPlayerPotentialMoveResult = currentPlayerPotentialMove.simulate(); 
                        if (!currentPlayerPotentialMoveResult.putsSelfInCheckmate()) {
                            this.isCheckmate = false; // Found a move that prevents checkmate
                            break; // Exit loop as we found a valid move
                        }
                    }
                }
            }
        }
        return chessboard;
    }

    public boolean isCheckmate() {
        return this.isCheckmate;
    }
}
