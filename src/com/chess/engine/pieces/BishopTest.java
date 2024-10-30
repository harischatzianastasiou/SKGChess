package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Tile.OccupiedTile;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BishopTest {

    public static void main(String[] args) {
   //Bishop legal moves vary depending the state of the tile(empty or occupied) in front of the bishop. So we have to test for both empty and occupied squares.
    	//testBishopMovesWithEmptyBoard();
    	testBishopMovesWithRandomOccupiedTiles(10,Alliance.WHITE);
    }

    public static void testBishopMovesWithEmptyBoard() {
    	
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
        
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
            Bishop bishop = new Bishop(tile, Alliance.WHITE);
            Collection<Move> legalMoves = bishop.calculateLegalMoves(board);
            System.out.println("Bishop on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    public static void testBishopMovesWithRandomOccupiedTiles(final int tileOccupiedByEnemyCoordinate, final Alliance tileOccupiedByEnemyAlliance) {
    	
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
    	Knight knight = new Knight(board.getTile(tileOccupiedByEnemyCoordinate), Alliance.BLACK);
    	Tile occupiedTile = Tile.createTile(tileOccupiedByEnemyCoordinate,tileOccupiedByEnemyAlliance, knight);
    	ALL_TILES.replace(tileOccupiedByEnemyCoordinate, occupiedTile);
    	
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
            Bishop bishop = new Bishop(tile, Alliance.WHITE);
            Collection<Move> legalMoves = bishop.calculateLegalMoves(board);
            System.out.println("Bishop on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
}