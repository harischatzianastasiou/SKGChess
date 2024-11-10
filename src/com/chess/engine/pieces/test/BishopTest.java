package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.Knight;
import java.util.Collection;
import java.util.Random;

public class BishopTest {

    public static void testBishopMovesWithStandardBoard() {
        Board board = Board.createStandardBoard();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            Tile currentTile = board.getTile(i);
            if (currentTile.isTileOccupied() && currentTile.getPiece() instanceof Bishop) {
                Bishop bishop = (Bishop) currentTile.getPiece();
                Collection<Move> legalMoves = bishop.calculateLegalMoves(board);
                System.out.print("Bishop on Tile " + i + " has " + legalMoves.size() + " legal moves ");
                if(legalMoves.size() > 0) {
                	System.out.print("--> ");
	                for (Move move : legalMoves) {
	                	System.out.print(move.getTargetCoordinate() + " ");          
	                }
                }
                System.out.print("\n");
            }
        }
    }
}