package com.chess.engine.pieces.test;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.engine.board.Board;
import com.chess.engine.board.moves.Move;
import com.chess.engine.pieces.Knight;

public class KnightTest {

	public static void testKnightMovesWithStandardBoard(Collection<Move> modifiedCollection,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : modifiedCollection) {
            	if(move.getPieceToMove() instanceof Knight && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Knight has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
	
	public static void testKnightMovesWithStandardBoard(Board board) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getPotentialLegalMoves()) {
            	if(move.getPieceToMove() instanceof Knight) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Knight has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}
