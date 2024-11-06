package com.chess.engine.pieces.test;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.board.Tile.OccupiedTile;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.King;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KingTest {

	private static final Random RANDOM = new Random();
	
    public static void main(String[] args) {
//    	testRankAndFile();
    	testKingMovesWithEmptyBoard();
//    	testKingMovesWithRandomOccupiedTiles(10);
    }

    public static void testKingMovesWithEmptyBoard() {
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            King king = new King(tileCoordinate, Alliance.WHITE);
            Collection<Move> legalMoves = king.calculateLegalMoves(board);
            System.out.println("King on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    public static void testKingMovesWithRandomOccupiedTiles(final int tileOccupiedByEnemyCoordinate) {
        Board board = new Board(); // Assuming a default constructor that initializes an empty board
    	Map<Integer, Tile> ALL_TILES = board.getAllTiles();
    	Knight knight = new Knight(tileOccupiedByEnemyCoordinate, Alliance.BLACK);
    	Tile occupiedTile = Tile.createTile(tileOccupiedByEnemyCoordinate, getRandomAlliance(), knight);
    	ALL_TILES.replace(tileOccupiedByEnemyCoordinate, occupiedTile);
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            King rook = new King(tileCoordinate, Alliance.WHITE);
            Collection<Move> legalMoves = rook.calculateLegalMoves(board);
            System.out.println("King on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
    
    private static Alliance getRandomAlliance() {
        Alliance[] alliances = Alliance.values();
        return alliances[RANDOM.nextInt(alliances.length)];
    }	
    
    public static void testRankAndFile() {
        for (int i = 0; i < 64; i++) {
            int rank = BoardUtils.getTileCoordinateRank(i);
            int file = BoardUtils.getTileCoordinateFile(i);
            System.out.printf("Tile %2d: Rank %d, File %d%n", i, rank, file);
        }
    }
}