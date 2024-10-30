package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Knight;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KnightTest {

    public static void main(String[] args) {
        testKnightMovesWithEmptyBoard();
    }

    public static void testKnightMovesWithEmptyBoard() {
    	
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> EMPTY_TILES_CACHE = BoardUtils.createAllPossibleEmptyTiles();
        
    	for (Map.Entry<Integer, Tile> entry : EMPTY_TILES_CACHE.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
            Knight knight = new Knight(tile, Alliance.WHITE);
            Collection<Move> legalMoves = knight.calculateLegalMoves(board);
            System.out.println("Knight on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
}
