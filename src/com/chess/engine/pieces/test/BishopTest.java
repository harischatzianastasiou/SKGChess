package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Pawn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class BishopTest {

	public static void testBishopMovesWithStandardBoard(Board board,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getLegalMoves()) {
            	if(move.getPieceToMove() instanceof Bishop && move.getPieceToMove().getPieceCoordinate() == coordinate) {
            		legalMoves.add(move);
            	}
            } 
            System.out.print("Bishop has " + legalMoves.size() + " legal moves ");
            if(legalMoves.size() > 0) {
            	System.out.print("--> ");
                for (Move move : legalMoves) {
                	System.out.print(move.getTargetCoordinate() + " ");          
                }
            }
            System.out.print("\n");
    }
}