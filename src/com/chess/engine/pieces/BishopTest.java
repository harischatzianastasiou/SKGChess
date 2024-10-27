package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BishopTest {

    public static void main(String[] args) {
        testBishopMoves();
    }

    public static void testBishopMoves() {
    	
    	Map<Integer, Tile> EMPTY_TILES_CACHE = BoardUtils.createAllPossibleEmptyTiles();
        
    	for (Map.Entry<Integer, Tile> entry : EMPTY_TILES_CACHE.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
   
            Bishop bishop = new Bishop(tile, Alliance.WHITE);
            Board board = new Board(); // Assuming a default constructor that initializes an empty board
            Collection<Move> legalMoves = bishop.calculateLegalMoves(board);
            System.out.println("Bishop on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
}