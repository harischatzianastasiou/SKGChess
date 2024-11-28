package com.chess.engine.pieces.test;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Queen;

public class QueenTest {

	public static void testQueenMovesWithStandardBoard(Collection<Move> modifiedCollection,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : modifiedCollection) {
            	if(move.getPieceToMove() instanceof Queen && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Queen has " + legalMoves.size() + " legal moves ");
            if(legalMoves.size() > 0) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
	
	public static void testQueenMovesWithStandardBoard(Board board) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getLegalMoves()) {
            	if(move.getPieceToMove() instanceof Queen) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Queen has " + legalMoves.size() + " legal moves ");
            if(legalMoves.size() > 0) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}