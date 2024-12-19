package com.chess.model.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chess.model.Alliance;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Knight;
import com.chess.model.pieces.Pawn;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Queen;
import com.chess.model.pieces.Rook;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;
import com.google.common.collect.ImmutableList;

public class Board implements IBoard {
	
	private final List<Tile> tiles;
    private final Player currentPlayer;
    private final Player opponentPlayer;

	private Board(final Builder builder) {
		this.tiles = createTiles(builder);
		this.opponentPlayer = Player.createPlayer(tiles, builder.currentPlayerAlliance.getOpposite(), null);
		this.currentPlayer = Player.createPlayer(tiles, builder.currentPlayerAlliance, this.opponentPlayer);
	}

	private static IBoard createBoard(Builder builder) {
        return new Board(builder);
    }
	
	private static List<Tile> createTiles(final Builder builder) {// list of 64 tiles. each occupied tile gets a piece mapped from the builder. if not mapped the tile is empty
	    final List<Tile> tiles = new ArrayList<>(CalculateMoveUtils.NUM_TILES);
	    for (int i = 0; i < CalculateMoveUtils.NUM_TILES; i++) {
	        Piece piece = builder.pieces.get(i);
	        if (builder.pieces.containsKey(i) && piece != null) {
	            tiles.add(Tile.createTile(i, CalculateMoveUtils.getCoordinateAlliance(i), piece));
	        } else {
	            tiles.add(Tile.createTile(i, CalculateMoveUtils.getCoordinateAlliance(i), null));
	        }
	    }
	    return ImmutableList.copyOf(tiles);
	}

	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < CalculateMoveUtils.NUM_TILES; i++) {
	        Tile tile = this.tiles.get(i);
	        if (tile.isTileOccupied()) {
	            Piece piece = tile.getPiece();
	            String pieceSymbol = piece.getPieceSymbol().toString();
	            String tileText = piece.getPieceAlliance().isWhite() ? pieceSymbol.toUpperCase() : pieceSymbol.toLowerCase();
	            builder.append(String.format("%3s", tileText));
	        } else {
	            builder.append(String.format("%3s", "-"));
	        }
	        if ((i + 1) % CalculateMoveUtils.NUM_TILES_PER_ROW == 0) {
	            builder.append("\n");
	        }
	    }
	    return builder.toString();
	}

	@Override
	public List<Tile> getTiles() {
		return ImmutableList.copyOf(tiles);
	}

	@Override
	public Tile getTile(final int tileCoordinate) {
        if (!CalculateMoveUtils.isCoordinateInBounds(tileCoordinate)) {
            throw new IllegalArgumentException("Invalid tile coordinate: " + tileCoordinate);
        }
        return tiles.get(tileCoordinate);
    }

	@Override
	public Player getCurrentPlayer() {
		return this.currentPlayer;
	}
	
	@Override
	public Player getOpponentPlayer() {
        return this.opponentPlayer;
	}

	public static class Builder{// Each time a Move is executed, we create a new Builder and build a new Board. --> Set mutable fields in Builder and once we call build(), we get an immutable Board object.
		
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
        
        public IBoard build() {//build a new board everytime a move is executed.
            return Board.createBoard(this);
        }
	}
	
	public static IBoard createStandardBoard() {//Create initial board
		// create white and black player here once
	    final Builder builder = new Builder();
	    
	    // Set up Black pieces
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
	    
	    // Set up White pieces
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
	    }
	    
	    // Set up current player's color
	    builder.setCurrentPlayerAlliance( Alliance.WHITE);

	    return createBoard(builder);
	}

	
}