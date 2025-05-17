package  com.chess.core.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.Bishop;
import  com.chess.core.pieces.CalculateMoveUtils;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Knight;
import  com.chess.core.pieces.Pawn;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Queen;
import  com.chess.core.pieces.Rook;
import  com.chess.core.player.Player;
import com.chess.core.tiles.Tile;
import com.chess.core.utils.FenUtils;
import com.google.common.collect.ImmutableList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.chess.core.moves.noncapturing.PawnJumpMove;

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

	/**
	 * Creates a board from a FEN string
	 * 
	 * @param fen The FEN string to parse
	 * @param lastMove The last move made
	 * @param isCastled Whether the current player has castled
	 * @return A new Board object representing the FEN position
	 */
	public static IBoard createBoardFromFEN(String fen, Move lastMove, boolean isCastled) {
		return FenUtils.fenToBoard(fen, lastMove, isCastled);
	}
	
	/**
	 * Converts the current board to a FEN string
	 * 
	 * @return FEN string representation of the board
	 */
	public String toFen() {
		return FenUtils.boardToFen(this);
	}

	/**
	 * Serializes the board to a JSON string for database storage
	 * 
	 * @return JSON string representation of the board
	 */
	public String serialize() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			// Configure mapper to handle circular references
			mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			
			// Create a direct representation of the Board class
			Map<String, Object> boardJson = new HashMap<>();
			
			// Add the main properties of the Board class
			boardJson.put("tiles", this.tiles);
			boardJson.put("currentPlayer", this.currentPlayer);
			boardJson.put("opponentPlayer", this.opponentPlayer);
			
			return mapper.writeValueAsString(boardJson);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize board", e);
		}
	}

	/**
	 * Serializes a move to a JSON string
	 * 
	 * @param move The move to serialize
	 * @return JSON string representation of the move
	 */
	public static String serializeMove(Move move) {
		if (move == null) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			// Configure mapper to handle circular references
			mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			
			// Create a direct representation of the Move class
			Map<String, Object> moveJson = new HashMap<>();
			
			// Add the main properties of the Move class
			moveJson.put("sourceCoordinate", move.getSourceCoordinate());
			moveJson.put("targetCoordinate", move.getTargetCoordinate());
			moveJson.put("pieceSymbol", move.getPieceToMove().getPieceSymbol().toString());
			moveJson.put("pieceAlliance", move.getPieceToMove().getPieceAlliance().toString());
			
			// Add the board tiles
			moveJson.put("boardTiles", move.getBoardTiles());
			
			// Add the move type
			if (move instanceof PawnJumpMove) {
				moveJson.put("moveType", "PAWN_JUMP");
			} else {
				moveJson.put("moveType", "UNKNOWN");
			}
			
			return mapper.writeValueAsString(moveJson);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize move", e);
		}
	}

	/**
	 * Deserializes a board from a JSON string
	 * 
	 * @param boardSerialized JSON string representation of the board
	 * @param lastMoveSerialized JSON string representation of the last move
	 * @param isCastled Whether the current player has castled
	 * @return A new Board object
	 */
	public static IBoard deserialize(String boardSerialized, String lastMoveSerialized, boolean isCastled) {
		if (boardSerialized == null || boardSerialized.trim().isEmpty()) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			// Parse the JSON string
			JsonNode rootNode = mapper.readTree(boardSerialized);
			
			// Create a new board builder
			Builder builder = new Builder();
			
			// Get the current player alliance from the currentPlayer node
			JsonNode currentPlayerNode = rootNode.get("currentPlayer");
			if (currentPlayerNode != null) {
				JsonNode allianceNode = currentPlayerNode.get("alliance");
				if (allianceNode != null) {
					String allianceStr = allianceNode.asText();
					Alliance alliance = Alliance.valueOf(allianceStr);
					builder.setcurrentPlayerAlliance(alliance);
				}
			}
			
			// Get pieces from the tiles node
			JsonNode tilesNode = rootNode.get("tiles");
			if (tilesNode != null && tilesNode.isArray()) {
				for (JsonNode tileNode : tilesNode) {
					JsonNode pieceNode = tileNode.get("piece");
					if (pieceNode != null && !pieceNode.isNull()) {
						int coordinate = tileNode.get("tileCoordinate").asInt();
						JsonNode allianceNode = pieceNode.get("pieceAlliance");
						JsonNode symbolNode = pieceNode.get("pieceSymbol");
						
						if (allianceNode != null && symbolNode != null) {
							Alliance pieceAlliance = Alliance.valueOf(allianceNode.asText());
							String symbol = symbolNode.asText();
							
							// Get isFirstMove from the piece node, default to true if not present
							boolean isFirstMove = true;
							if (pieceNode.has("firstMove")) {
								isFirstMove = pieceNode.get("firstMove").asBoolean();
							}
							
							// Create the appropriate piece based on the symbol with its first move status
							Piece piece = null;
							switch (symbol) {
								case "PAWN":
									piece = new Pawn(coordinate, pieceAlliance, isFirstMove);
									break;
								case "KNIGHT":
									piece = new Knight(coordinate, pieceAlliance, isFirstMove);
									break;
								case "BISHOP":
									piece = new Bishop(coordinate, pieceAlliance, isFirstMove);
									break;
								case "ROOK":
									piece = new Rook(coordinate, pieceAlliance, isFirstMove);
									break;
								case "QUEEN":
									piece = new Queen(coordinate, pieceAlliance, isFirstMove);
									break;
								case "KING":
									piece = new King(coordinate, pieceAlliance, isFirstMove);
									break;
							}
							
							if (piece != null) {
								builder.setPiece(piece);
							}
						}
					}
				}
			}
			
			// Deserialize the last move if provided
			Move lastMove = null;
			if (lastMoveSerialized != null && !lastMoveSerialized.isEmpty()) {
				lastMove = deserializeMove(lastMoveSerialized);
			}
			
			// Build and return the board with the provided lastMove and isCastled parameters
			return builder.build(lastMove, isCastled);
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize board", e);
		}
	}


	/**
	 * Deserializes a move from a JSON string
	 * 
	 * @param moveSerialized JSON string representation of the move
	 * @return A Move object
	 */
	private static Move deserializeMove(String moveSerialized) {
		if (moveSerialized == null || moveSerialized.isEmpty()) {
			return null;
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode moveNode = objectMapper.readTree(moveSerialized);
			
			// Get common properties
			int sourceCoordinate = moveNode.get("sourceCoordinate").asInt();
			int targetCoordinate = moveNode.get("targetCoordinate").asInt();
			String pieceSymbol = moveNode.get("pieceSymbol").asText();
			String allianceStr = moveNode.get("pieceAlliance").asText();
			String moveType = moveNode.get("moveType").asText();
			
			// Convert string alliance to Alliance enum
			Alliance pieceAlliance = Alliance.valueOf(allianceStr);
			
			// Create the appropriate move based on moveType
			switch (moveType) {
				case "PAWN_JUMP":
					Piece piece = new Pawn(targetCoordinate, pieceAlliance, true);
					
					// Get the board tiles from the move data
					JsonNode boardTilesNode = moveNode.get("boardTiles");
					List<Tile> boardTiles;
					
					if (boardTilesNode != null && !boardTilesNode.isNull()) {
						// If boardTiles is provided in the JSON, deserialize it
						// This would require a custom deserializer for List<Tile>
						// For now, we'll create a standard board
						IBoard tempBoard = createStandardBoard();//TODO: change to the board that was serialized
						boardTiles = tempBoard.getTiles();
					} else {
						// If no boardTiles provided, create a standard board
						IBoard tempBoard = createStandardBoard();
						boardTiles = tempBoard.getTiles();
					}
					
					return new PawnJumpMove(boardTiles, sourceCoordinate, targetCoordinate, piece);
				default:
					return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize move", e);
		}
	}
}