package  com.chess.core.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import  com.chess.core.Alliance;
import  com.chess.core.pieces.Bishop;
import  com.chess.core.pieces.CalculateMoveUtils;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Knight;
import  com.chess.core.pieces.Pawn;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Queen;
import  com.chess.core.pieces.Rook;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;
import com.google.common.collect.ImmutableList;
import com.chess.core.moves.Move;

public class Board implements IBoard {
	
	private final List<Tile> tiles;
    private final Player currentPlayer;
    private final Player opponentPlayer;

	private Board(final Builder builder) {
		this.tiles = createTiles(builder);
		this.opponentPlayer = Player.createPlayer(tiles, builder.currentPlayerAlliance.getOpposite(),null, null, false);
		this.currentPlayer = Player.createPlayer(tiles, builder.currentPlayerAlliance, this.opponentPlayer, null, false);
	}

	private Board(final Builder builder, final Move lastMove, final boolean isCastled) {
        this.tiles = createTiles(builder);
        this.opponentPlayer = Player.createPlayer(tiles, builder.currentPlayerAlliance.getOpposite(),null, null, false);
        this.currentPlayer = Player.createPlayer(tiles, builder.currentPlayerAlliance, this.opponentPlayer, lastMove, isCastled);
    }

	private static IBoard createBoard(Builder builder) {
        return new Board(builder);
    }

	private static IBoard createBoard(Builder builder, Move lastMove, boolean isCastled) {
        return new Board(builder, lastMove, isCastled);
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
		
		public Builder setcurrentPlayerAlliance(final Alliance currentPlayerAlliance) {
            this.currentPlayerAlliance = currentPlayerAlliance;
            return this;
        }
        
        public IBoard build(Move lastMove, boolean isCastled) {//build a new board everytime a move is executed.
            return Board.createBoard(this, lastMove, isCastled);
        }

		public IBoard build() {//build a new board everytime a move is executed.
            return Board.createBoard(this);
        }
	}
	
	public static IBoard createStandardBoard() {//Create initial board
		// create white and black user here once
	    final Builder builder = new Builder();
	    
	    //Set up Black pieces
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
	   
	    // Set up current user's color
	    builder.setcurrentPlayerAlliance( Alliance.WHITE);

	    return createBoard(builder);
	}

    public static IBoard createEmptyBoard() {
        final Builder builder = new Builder();
        
        // Set the first move to WHITE
        builder.setcurrentPlayerAlliance(Alliance.WHITE);
        
	    return createBoard(builder);
    }

    public static IBoard createRandomBoard(Collection<Piece> pieces ) {
        final Builder builder = new Builder();
        
        // Place the specified pieces
        for (final Piece piece : pieces) {
            int coordinate = piece.getPieceCoordinate();
			Alliance alliance = piece.getPieceAlliance();
            
            switch (piece.getPieceSymbol()) {
                case ROOK:
                    builder.setPiece(new Rook(coordinate, alliance));
                    break;
                case KNIGHT:
                    builder.setPiece(new Knight(coordinate, alliance));
                    break;
                case BISHOP:
                    builder.setPiece(new Bishop(coordinate, alliance));
                    break;
                case QUEEN:
                    builder.setPiece(new Queen(coordinate, alliance));
                    break;
                case KING:
                    builder.setPiece(new King(coordinate, alliance));
                    break;
                case PAWN:
                    builder.setPiece(new Pawn(coordinate, alliance));
                    break;
            }
        }
        
        // Set the move to WHITE
        builder.setcurrentPlayerAlliance(Alliance.WHITE);
        
	    return createBoard(builder);
    }

	public Collection<Piece> getAllPieces() {
		return this.tiles.stream()
				.map(Tile::getPiece)
				.filter(piece -> piece != null)
				.toList();
	}

	public static IBoard createBoardFromFEN(String fen, Move lastMove, boolean isCastled) {
		if (fen == null || fen.trim().isEmpty()) {
			return null;
		}

		String[] fenParts = fen.split(" ");
		String piecePlacement = fenParts[0];
		Alliance currentPlayerAlliance = fenParts[1].equals("w") ? Alliance.WHITE : Alliance.BLACK;

		Builder builder = new Builder();
		builder.setcurrentPlayerAlliance(currentPlayerAlliance);

		int rank = 7; // Start from top rank (7)
		int file = 0; // Start from a-file (0)
		
		for (char c : piecePlacement.toCharArray()) {
			if (c == '/') {
				rank--;
				file = 0;
			} else if (Character.isDigit(c)) {
				file += Character.getNumericValue(c);
			} else {
				int coordinate = rank * 8 + file;
				Alliance alliance = Character.isUpperCase(c) ? Alliance.WHITE : Alliance.BLACK;
				char pieceChar = Character.toUpperCase(c);
				
				switch (pieceChar) {
					case 'P': builder.setPiece(new Pawn(coordinate, alliance)); break;
					case 'R': builder.setPiece(new Rook(coordinate, alliance)); break;
					case 'N': builder.setPiece(new Knight(coordinate, alliance)); break;
					case 'B': builder.setPiece(new Bishop(coordinate, alliance)); break;
					case 'Q': builder.setPiece(new Queen(coordinate, alliance)); break;
					case 'K': builder.setPiece(new King(coordinate, alliance)); break;
				}
				file++;
			}
		}
		
		return builder.build(lastMove, isCastled);
	}
}