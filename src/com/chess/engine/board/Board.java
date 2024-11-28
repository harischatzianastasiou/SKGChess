package com.chess.engine.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.pieces.*;
import com.chess.engine.player.Player;
import com.chess.engine.player.PlayerFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class Board {
	
	private final List<Tile> tiles;
    private Player currentPlayer;
    private Player opponentPlayer;
    
	private Board(final Builder builder) {
		this.tiles = createTiles(builder);
        this.currentPlayer = PlayerFactory.createPlayer(tiles, builder.currentPlayerAlliance);
        this.opponentPlayer = PlayerFactory.createPlayer(tiles, currentPlayer.getOpponentAlliance());
	}
	
	private Board(final Builder builder, final Move move) {// called when a move is made, create a new board and add to GameHistory
		this.tiles = createTiles(builder);
		this.currentPlayer = PlayerFactory.createPlayer(tiles, builder.currentPlayerAlliance);
	    this.opponentPlayer = PlayerFactory.createPlayer(tiles, currentPlayer.getOpponentAlliance());
        GameHistory.getInstance().addBoardState(this);
        GameHistory.getInstance().addMove(move);
	}
	
	private static List<Tile> createTiles(final Builder builder) {
	    final List<Tile> tiles = new ArrayList<>(BoardUtils.NUM_TILES);
	    for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
	        Piece piece = builder.pieces.get(i);
	        if (builder.pieces.containsKey(i) && piece != null) {
	            tiles.add(Tile.createTile(i, BoardUtils.getCoordinateAlliance(i), piece));
	        } else {
	            tiles.add(Tile.createTile(i, BoardUtils.getCoordinateAlliance(i), null));
	        }
	    }
	    return ImmutableList.copyOf(tiles);
	}
	
	public static Board createStandardBoard() {
		// create white and black player here once
	    final Builder builder = new Builder();
	    
	    // Set up White pieces
	    builder.setPiece(new Rook  (0, Alliance.BLACK));
	    builder.setPiece(new Knight(1, Alliance.BLACK));
	    builder.setPiece(new Bishop(2, Alliance.BLACK));
	    builder.setPiece(new Queen (3, Alliance.BLACK));
	    builder.setPiece(new King  (4, Alliance.BLACK));
	    builder.setPiece(new Bishop(5, Alliance.BLACK));
	    builder.setPiece(new Knight(6, Alliance.BLACK));
	    builder.setPiece(new Rook  (7, Alliance.BLACK));
	    for(int coordinate = 8; coordinate < 16; coordinate++) {
	        builder.setPiece(new Pawn(coordinate, Alliance.BLACK));
	    }
	    
	    // Set up Black pieces
	    builder.setPiece(new Rook  (56, Alliance.WHITE));
	    builder.setPiece(new Knight(57, Alliance.WHITE));
	    builder.setPiece(new Bishop(58, Alliance.WHITE));
	    builder.setPiece(new Queen (59, Alliance.WHITE));
	    builder.setPiece(new King  (60, Alliance.WHITE));
	    builder.setPiece(new Bishop(61, Alliance.WHITE));
	    builder.setPiece(new Knight(62, Alliance.WHITE));
	    builder.setPiece(new Rook  (63, Alliance.WHITE));
	    for(int coordinate = 48; coordinate < 56; coordinate++) {
	        builder.setPiece(new Pawn(coordinate, Alliance.WHITE));
	    }//oxi mono gia ton king alla kai gia alla pieces lambanei ta tiles san oocupied
	    
	    // Set up next player
	    builder.setCurrentPlayerAlliance( Alliance.WHITE);

	    return createBoard(builder);
	}
	
	public static class Builder{//Set mutable fields in Builder and once we call build(), we get an immutable Board object.
		
		private Map<Integer, Piece> pieces;
		private Alliance currentPlayerAlliance;

		public Builder() {
			this.pieces = new HashMap<>(32, 1.0f);
        }
		
		public Builder setPiece(final Piece piece) {
			this.pieces.put(piece.getPieceCoordinate(), piece);
			return this;
		}
		
		public Builder setCurrentPlayerAlliance(final Alliance currentPlayerAlliance) {
            this.currentPlayerAlliance = currentPlayerAlliance;
            return this;
        }
		
		public Map<Integer,Piece> getPieces() {
			return pieces;
		}
		
        public Board build() {// build the 1st board
            return Board.createBoard(this);
        }
        
        public Board build(Move move) {//build a new board everytime a move is executed
            return Board.createBoard(this, move);
        }
	}
	
    private static Board createBoard(Builder builder) {
        return new Board(builder);
    }
    
    private static Board createBoard(Builder builder, Move move) {
        return new Board(builder, move);
    }
   
	public List<Tile> getTiles() {
		return ImmutableList.copyOf(tiles);
	}
	
	public Tile getTile(final int tileCoordinate) {
        return tiles.get(tileCoordinate);
    }
	public Player getCurrentPlayer() {
		return this.currentPlayer;
	}
	
	public Player getOpponentPlayer() {
        return this.opponentPlayer;
    }
	
	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
	        Tile tile = this.tiles.get(i);
	        if (tile.isTileOccupied()) {
	            Piece piece = tile.getPiece();
	            String pieceSymbol = piece.getPieceSymbol().toString();
	            String tileText = piece.getPieceAlliance().isWhite() ? pieceSymbol.toUpperCase() : pieceSymbol.toLowerCase();
	            builder.append(String.format("%3s", tileText));
	        } else {
	            builder.append(String.format("%3s", "-"));
	        }
	        if ((i + 1) % BoardUtils.NUM_TILES_PER_ROW == 0) {
	            builder.append("\n");
	        }
	    }
	    return builder.toString();
	}

	
//	memoization-make it new  class
	public class BoardCache {
	    private static final Map<String, Board> boardCache = new HashMap<>();

	    public static Board getBoard(Board.Builder builder) {
	        String boardKey = generateBoardKey(builder);
	        return boardCache.computeIfAbsent(boardKey, k -> Board.createBoard(builder));
	    }

	    private static String generateBoardKey(Board.Builder builder) {
	        // Generate a unique key based on the board configuration
	        StringBuilder key = new StringBuilder();
	        for (Map.Entry<Integer, Piece> entry : builder.getPieces().entrySet()) {
	            key.append(entry.getKey()).append(":").append(entry.getValue().toString()).append("|");
	        }
	        key.append(builder.currentPlayerAlliance);
	        return key.toString();
	    }
	}
}



/*
The Builder class in this Board class is an implementation of the Builder pattern, which is discussed in detail in Joshua Bloch's "Effective Java" book (Item 2 in the 3rd edition: "Consider a builder when faced with many constructor parameters").

Here's how the Builder pattern helps in this context:

1.
Handling Complex Object Creation: The Board class likely has many components (tiles, pieces, game state, etc.). Using a Builder can simplify the process of creating a Board object with many parameters.
2.
Flexibility: It allows for creating different configurations of the Board without needing multiple constructors or a single constructor with many parameters.
3.
Readability: When using the Builder pattern, the code for creating a Board object becomes more readable, as each setter method in the Builder can be named descriptively.
4.
Immutability: The Builder pattern can help in creating immutable Board objects. Once the Board is built, its state cannot be changed.
5.
Step-by-step Construction: It allows for step-by-step construction of the Board, which can be useful if the board setup process is complex or involves multiple steps.
6.
Default Values: The Builder can set default values for certain parameters, making it easier to create a Board with only the necessary custom settings.
7.
Validation: The Builder can perform validation on the parameters before actually creating the Board object.
*/