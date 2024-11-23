package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Queen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QueenTest {

	public static void testQueenMovesWithStandardBoard(Board board,int coordinate) {
		Collection<Move> legalMoves = new ArrayList<>();
			for(Move move : board.getCurrentPlayer().getLegalMoves()) {
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
}