package com.chess.engine.pieces.test;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Rook;

public class RookTest {

	public static void testRookMovesWithStandardBoard(Collection<Move> modifiedCollection,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : modifiedCollection) {
            	if(move.getPieceToMove() instanceof Rook && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Rook has " + legalMoves.size() + " legal moves ");
            if(legalMoves.size() > 0) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
	
	public static void testRookMovesWithStandardBoard(Board board) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getLegalMoves()) {
            	if(move.getPieceToMove() instanceof Rook) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Rook has " + legalMoves.size() + " legal moves ");
            if(legalMoves.size() > 0) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}