package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Rook;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RookTest {

	private static final Random RANDOM = new Random();
	
    public static void main(String[] args) {
   //Rook legal moves vary depending the state of the tile(empty or occupied) in front of the bishop. So we have to test for both empty and occupied squares.
//    	testRookMovesWithEmptyBoard();
    	testRookMovesWithRandomOccupiedTiles(10);
    }

    public static void testRookMovesWithEmptyBoard() {
    	
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
        
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
            Rook rook = new Rook(tile, Alliance.WHITE);
            Collection<Move> legalMoves = rook.calculateLegalMoves(board);
            System.out.println("Rook on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    public static void testRookMovesWithRandomOccupiedTiles(final int tileOccupiedByEnemyCoordinate) {
    	
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
    	Knight knight = new Knight(board.getTile(tileOccupiedByEnemyCoordinate), Alliance.BLACK);
    	Tile occupiedTile = Tile.createTile(tileOccupiedByEnemyCoordinate, getRandomAlliance(), knight);
    	ALL_TILES.replace(tileOccupiedByEnemyCoordinate, occupiedTile);
    	
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
            Rook rook = new Rook(tile, Alliance.WHITE);
            Collection<Move> legalMoves = rook.calculateLegalMoves(board);
            System.out.println("Rook on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    private static Alliance getRandomAlliance() {
        Alliance[] alliances = Alliance.values();
        return alliances[RANDOM.nextInt(alliances.length)];
    }
    
//    must add cases for all tests like --> 1. testing with no occupied tiles(knight,bishop,rook)
    
//									            the two tiles that we are testing are of the same color
//									            the two tiles that we are testing are not of the same color
//    
//    										2. when color of tiles is important (knight,bishop) then 
//    										
//	    										testing with an occupied tile of the same color by an enemy
//	    										testing with an occupied tile of different color by an enemy
//	    										testing with an occupied tile of the same color by an ally
//	    										testing with an occupied tile of different color by an ally
//    										
//    										3. when color of tile is not important then
//    										
//	    										testing with an occupied tile of the same color by an enemy
//	    										testing with an occupied tile of different color by an enemy
    										
}