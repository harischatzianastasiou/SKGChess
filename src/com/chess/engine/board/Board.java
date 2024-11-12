package com.chess.engine.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.pieces.*;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class Board {
	
	private final List<Tile> tiles;
	private final Collection<Piece>	whitePieces;
	private final Collection<Piece> blackPieces;
	private final Collection<Move>  whiteLegalMoves;
	private final Collection<Move>  blackLegalMoves;
	private final Player whitePlayer;
	private final Player blackPlayer;
	
	private Board(Builder builder) {
		this.tiles = createTiles(builder);
		this.whitePieces = calculateActivePieces(this.tiles, Alliance.WHITE);
		this.blackPieces = calculateActivePieces(this.tiles, Alliance.BLACK);
		this.whiteLegalMoves = calculateLegalMoves(this.whitePieces);
		this.blackLegalMoves = calculateLegalMoves(this.blackPieces);
		this.whitePlayer = new WhitePlayer(this, whiteLegalMoves, blackLegalMoves);
		this.blackPlayer = new BlackPlayer(this, blackLegalMoves, whiteLegalMoves);
	}
	
    public static Board createBoard(Builder builder) {
        return new Board(builder);
    }
	
	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
	        Tile tile = this.tiles.get(i);
	        String tileText = tile.isTileOccupied() ? tile.getPiece().toString() : "-";
	        builder.append(String.format("%3s", tileText));
	        if ((i + 1) % BoardUtils.NUM_TILES_PER_ROW == 0) {
	            builder.append("\n");
	        }
	    }
	    return builder.toString();
	}
	private static Collection<Piece> calculateActivePieces(final List<Tile> tiles, Alliance alliance) {
		final List<Piece> activePieces = new ArrayList<>();
		for(final Tile tile : tiles) {
			if(tile.isTileOccupied()) {
				final Piece piece = tile.getPiece();
				if(piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                }
			}
			
		}
		return ImmutableList.copyOf(activePieces);
	}
	
	private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {
		final List<Move> legalMoves = new ArrayList();
		for(final Tile tile : tiles){
				if(tile.isTileOccupied()){
					final Piece piece = tile.getPiece();
						legalMoves.addAll(piece.calculateLegalMoves(this));
				}	
		}
		return ImmutableList.copyOf(legalMoves);
	}

	private static List<Tile> createTiles(Builder builder) {
		final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];        
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles[i] = Tile.createTile(i, BoardUtils.getCoordinateAlliance(i), builder.startingPieces.get(i));
        }
        return ImmutableList.copyOf(tiles);
    }
	
	public static Board createStandardBoard() {
	    final Builder builder = new Builder();
	    
	    // Set up White pieces
	    builder.setPiece(new Rook  (0, Alliance.WHITE));
	    builder.setPiece(new Knight(1, Alliance.WHITE));
	    builder.setPiece(new Bishop(2, Alliance.WHITE));
	    builder.setPiece(new Queen (3, Alliance.WHITE));
	    builder.setPiece(new King  (4, Alliance.WHITE));
	    builder.setPiece(new Bishop(5, Alliance.WHITE));
	    builder.setPiece(new Knight(6, Alliance.WHITE));
	    builder.setPiece(new Rook  (7, Alliance.WHITE));
	    for(int coordinate = 8; coordinate < 16; coordinate++) {
	        builder.setPiece(new Pawn(coordinate, Alliance.WHITE));
	    }
	    
	    // Set up Black pieces
	    builder.setPiece(new Rook  (56, Alliance.BLACK));
	    builder.setPiece(new Knight(57, Alliance.BLACK));
	    builder.setPiece(new Bishop(58, Alliance.BLACK));
	    builder.setPiece(new Queen (59, Alliance.BLACK));
	    builder.setPiece(new King  (60, Alliance.BLACK));
	    builder.setPiece(new Bishop(61, Alliance.BLACK));
	    builder.setPiece(new Knight(62, Alliance.BLACK));
	    builder.setPiece(new Rook  (63, Alliance.BLACK));
	    for(int coordinate = 48; coordinate < 56; coordinate++) {
	        builder.setPiece(new Pawn(coordinate, Alliance.BLACK));
	    }
	    
	    // Set the first move to White
	    builder.setMovePlayer(Alliance.WHITE);
	    
	    // Build and return the board
        return createBoard(builder);
	}
	
	public static class Builder{//Set mutable fields in Builder and once we call build(), we get an immutable Board object.
		
		private Map<Integer, Piece> startingPieces;
		private Alliance nextMovePlayer;
		
		public Builder() {
			this.startingPieces = new HashMap<>();
        }
		
		public Builder setPiece(final Piece piece) {
			this.startingPieces.put(piece.getPieceCoordinate(), piece);
			return this;
		}
		
		public Builder setMovePlayer(final Alliance nextMovePlayer) {
			this.nextMovePlayer = nextMovePlayer;
            return this;		
		}
		
		public Map<Integer,Piece> getStartingPieces() {
			return startingPieces;
		}
		
		public Alliance getNextMovePlayer() {
            return nextMovePlayer;
        }
		
        public Board build() {
            return Board.createBoard(this);
        }	
	}
	

	public Tile getTile(final int tileCoordinate) {
        return tiles.get(tileCoordinate);
    }
	
	public Collection<Piece> getWhitePieces() {
		return whitePieces;
	}
	
	public Collection<Piece> getBlackPieces() {
        return blackPieces;
	}
	
	public Player getWhitePlayer() {
		return this.whitePlayer;
	}
	
	public Player getBlackPlayer() {
        return this.blackPlayer;
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