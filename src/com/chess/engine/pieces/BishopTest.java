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
//    	testBishopMovesForEmptySquares();
    	testBishopMovesForOccupiedSquares(10,Alliance.WHITE);
    }

    public static void testBishopMovesForEmptySquares() {
    	
    	//must add scenarios where the are blocking pieces in the direction a bishop looks at.
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
    
    public static void testBishopMovesForOccupiedSquares(final int tileOccupiedByEnemyCoordinate, final Alliance tileOccupiedByEnemyAlliance) {
    	
    	//must add scenarios where the are blocking pieces in the direction a bishop looks at.
    	Map<Integer, Tile> ALL_TILES = Board.ALL_TILES;
        
    	for (Map.Entry<Integer, Tile> entry : ALL_TILES.entrySet()) {
            int tileCoordinate = entry.getKey();
            Tile tile = entry.getValue();
   
            Bishop bishop = new Bishop(tile, Alliance.WHITE);
            Board board = new Board(); // Assuming a default constructor that initializes an empty board

            Knight knight = new Knight(board.getTile(tileOccupiedByEnemyCoordinate), Alliance.BLACK);
            Tile occupiedTile = Tile.createTile(tileOccupiedByEnemyCoordinate,tileOccupiedByEnemyAlliance, knight);
            ALL_TILES.replace(tileOccupiedByEnemyCoordinate, occupiedTile);
            
            Collection<Move> legalMoves = bishop.calculateLegalMoves(board);
            System.out.println("Bishop on Tile " + tileCoordinate + " has the above " + legalMoves.size() + " legal moves.\n");
        }
    }
}