package com.chess.engine.pieces.test;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;

public class KingTest {

	public static void testKingMovesWithStandardBoard(Collection<Move> modifiedCollection,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : modifiedCollection) {
            	if(move.getPieceToMove() instanceof King && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("King has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
	
	public static void testKingMovesWithStandardBoard(Board board) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getLegalMoves()) {
            	if(move.getPieceToMove() instanceof King) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("King has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}