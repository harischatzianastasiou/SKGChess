package  com.chess.core.board;

import java.util.Collection;
import java.util.List;

import  com.chess.core.pieces.Piece;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;
import com.chess.pgn.FENUtils;

public interface IBoard {
    List<Tile> getTiles();
    Tile getTile(int tileCoordinate);
    Player getCurrentPlayer();
    Player getOpponentPlayer();
    
    /**
     * Gets the FEN (Forsyth-Edwards Notation) string representation of the current board position.
     * This is used to uniquely identify positions for the opening book.
     * @return The FEN string for this position
     */
    default String getFEN() {
        return FENUtils.toFEN(this);
    }

    /**
     * Creates a standard chess board with all pieces in their initial positions.
     * @return A new board with standard piece setup
     */
    static IBoard createStandardBoard() {
        return Board.createStandardBoard();
    }
    
    /**
     * Creates an empty chess board with no pieces.
     * @return A new empty board
     */
    static IBoard createEmptyBoard() {
        return Board.createEmptyBoard();
    }
    
    /**
     * Creates an empty chess board and places the specified pieces on it.
     * @param pieces Collection of pieces to place on the board
     * @return A new board with only the specified pieces
     */
    static IBoard createRandomBoard(Collection<Piece> pieces) {
        return Board.createRandomBoard(pieces);
    }

    Collection<Piece> getAllPieces();
} 