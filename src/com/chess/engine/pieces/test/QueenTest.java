package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Queen;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QueenTest {

	private static final Random RANDOM = new Random();
	
    public static void main(String[] args) {
//    	testQueenMovesWithEmptyBoard();
    	testQueenMovesWithRandomOccupiedTiles(10);
    }

    public static void testQueenMovesWithEmptyBoard() {
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Queen Queen = new Queen(tileCoordinate, Alliance.WHITE);
            Collection<Move> legalMoves = Queen.calculateLegalMoves(board);
            System.out.println("Queen on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    public static void testQueenMovesWithRandomOccupiedTiles(final int tileOccupiedByEnemyCoordinate) {
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
    	Knight knight = new Knight(tileOccupiedByEnemyCoordinate, Alliance.BLACK);
    	Tile occupiedTile = Tile.createTile(tileOccupiedByEnemyCoordinate, getRandomAlliance(), knight);
    	ALL_TILES.replace(tileOccupiedByEnemyCoordinate, occupiedTile);
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Queen Queen = new Queen(tileCoordinate, Alliance.WHITE);
            Collection<Move> legalMoves = Queen.calculateLegalMoves(board);
            System.out.println("Queen on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    private static Alliance getRandomAlliance() {
        Alliance[] alliances = Alliance.values();
        return alliances[RANDOM.nextInt(alliances.length)];
    }									
}