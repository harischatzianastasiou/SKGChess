package com.chess.engine.pieces.test;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Pawn;

public class PawnTest {

	public static void testPawnMovesWithStandardBoard(Collection<Move> modifiedCollection,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : modifiedCollection) {
            	if(move.getPieceToMove() instanceof Pawn && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
	
	public static void testPawnMovesWithStandardBoard(Board board) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getLegalMoves()) {
            	if(move.getPieceToMove() instanceof Pawn) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Pawn has " + legalMoves.size() + " legal moves ");
            if(!legalMoves.isEmpty()) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}
