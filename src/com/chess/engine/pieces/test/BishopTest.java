package com.chess.engine.pieces.test;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.engine.board.Board;
import com.chess.engine.board.moves.Move;
import com.chess.engine.pieces.Bishop;

public class BishopTest {

	public static void testBishopMovesWithStandardBoard(Collection<Move> modifiedCollection,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : modifiedCollection) {
            	if(move.getPieceToMove() instanceof Bishop && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Bishop has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
	
	public static void testBishopMovesWithStandardBoard(Board board) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getPotentialLegalMoves()) {
            	if(move.getPieceToMove() instanceof Bishop) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Bishop has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}