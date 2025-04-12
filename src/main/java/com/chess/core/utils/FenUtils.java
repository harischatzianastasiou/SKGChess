package com.chess.core.utils;

import java.util.HashMap;
import java.util.Map;

import com.chess.core.Alliance;
import com.chess.core.board.Board;
import com.chess.core.board.IBoard;
import com.chess.core.pieces.Piece;
import com.chess.core.moves.Move;

/**
 * Utility class for converting between FEN (Forsyth–Edwards Notation) and Board objects.
 * FEN is a standard notation for describing a particular board position of a chess game.
 */
public class FenUtils {

    // Map to convert piece types to FEN characters
    private static final Map<Class<? extends Piece>, Character> PIECE_TO_FEN = new HashMap<>();
    
    // Initialize the piece to FEN character mapping
    static {
        PIECE_TO_FEN.put(com.chess.core.pieces.Pawn.class, 'P');
        PIECE_TO_FEN.put(com.chess.core.pieces.Rook.class, 'R');
        PIECE_TO_FEN.put(com.chess.core.pieces.Knight.class, 'N');
        PIECE_TO_FEN.put(com.chess.core.pieces.Bishop.class, 'B');
        PIECE_TO_FEN.put(com.chess.core.pieces.Queen.class, 'Q');
        PIECE_TO_FEN.put(com.chess.core.pieces.King.class, 'K');
    }

    /**
     * Converts a Board object to FEN notation
     * 
     * @param board The board to convert
     * @return FEN string representation of the board
     */
    public static String boardToFen(IBoard board) {
        if (board == null) {
            return "";
        }

        StringBuilder fen = new StringBuilder();
        
        // Segment 1: Piece placement
        fen.append(getPiecePlacement(board));
        
        // Segment 2: Active color
        fen.append(" ").append(board.getCurrentPlayer().getAlliance() == Alliance.WHITE ? "w" : "b");
        
        // Segment 3: Castling availability
        fen.append(" ").append(getCastlingAvailability(board));
        
        // Segment 4: En passant target square
        fen.append(" ").append(getEnPassantTargetSquare(board));
        
        // Segment 5: Halfmove clock (not implemented in current board)
        fen.append(" 0");
        
        // Segment 6: Fullmove number (not implemented in current board)
        fen.append(" 1");
        
        return fen.toString();
    }

    /**
     * Gets the piece placement part of the FEN string
     * 
     * @param board The board to convert
     * @return Piece placement string
     */
    private static String getPiecePlacement(IBoard board) {
        StringBuilder placement = new StringBuilder();
        
        // Iterate through ranks (8 to 1)
        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            
            // Iterate through files (a to h)
            for (int file = 0; file < 8; file++) {
                int coordinate = rank * 8 + file;
                Piece piece = board.getTile(coordinate).getPiece();
                
                if (piece == null) {
                    emptySquares++;
                } else {
                    // If we had empty squares before this piece, add the count
                    if (emptySquares > 0) {
                        placement.append(emptySquares);
                        emptySquares = 0;
                    }
                    
                    // Add the piece character (uppercase for white, lowercase for black)
                    char pieceChar = PIECE_TO_FEN.get(piece.getClass());
                    if (piece.getPieceAlliance() == Alliance.BLACK) {
                        pieceChar = Character.toLowerCase(pieceChar);
                    }
                    placement.append(pieceChar);
                }
            }
            
            // If we have empty squares at the end of the rank, add the count
            if (emptySquares > 0) {
                placement.append(emptySquares);
            }
            
            // Add rank separator (except for the last rank)
            if (rank > 0) {
                placement.append("/");
            }
        }
        
        return placement.toString();
    }

    /**
     * Gets the castling availability part of the FEN string
     * 
     * @param board The board to convert
     * @return Castling availability string
     */
    private static String getCastlingAvailability(IBoard board) {
        // This is a simplified implementation
        // In a real implementation, you would check if kings and rooks have moved
        // and if there are pieces between them
        
        // For now, we'll assume castling is available for both sides
        return "KQkq";
    }

    /**
     * Gets the en passant target square part of the FEN string
     * 
     * @param board The board to convert
     * @return En passant target square string
     */
    private static String getEnPassantTargetSquare(IBoard board) {
        // This is a simplified implementation
        // In a real implementation, you would check if a pawn has just made a two-square move
        
        // For now, we'll assume there is no en passant target square
        return "-";
    }

    /**
     * Parses a FEN string and returns a Board object
     * 
     * @param fen The FEN string to parse
     * @return A new Board object representing the FEN position
     */
    public static IBoard fenToBoard(String fen, Move lastMove, boolean isCastled) {
        if (fen == null || fen.trim().isEmpty()) {
            return null;
        }

        // Split the FEN string into its components
        String[] fenParts = fen.split(" ");
        
        // Check if we have at least the piece placement and active color
        if (fenParts.length < 2) {
            return null;
        }

        // Get the piece placement
        String piecePlacement = fenParts[0];
        
        // Get the active color
        Alliance currentPlayerAlliance = fenParts[1].equals("w") ? Alliance.WHITE : Alliance.BLACK;
        
        // Create a new board builder
        Board.Builder builder = new Board.Builder();
        builder.setcurrentPlayerAlliance(currentPlayerAlliance);
        
        // Parse the piece placement
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
                    case 'P': builder.setPiece(new com.chess.core.pieces.Pawn(coordinate, alliance)); break;
                    case 'R': builder.setPiece(new com.chess.core.pieces.Rook(coordinate, alliance)); break;
                    case 'N': builder.setPiece(new com.chess.core.pieces.Knight(coordinate, alliance)); break;
                    case 'B': builder.setPiece(new com.chess.core.pieces.Bishop(coordinate, alliance)); break;
                    case 'Q': builder.setPiece(new com.chess.core.pieces.Queen(coordinate, alliance)); break;
                    case 'K': builder.setPiece(new com.chess.core.pieces.King(coordinate, alliance)); break;
                }
                file++;
            }
        }
        
        // Build and return the board
        return builder.build(lastMove, isCastled);
    }
    
    /**
     * Converts a board coordinate to algebraic notation (e.g., 0 -> a1, 63 -> h8)
     * 
     * @param coordinate The board coordinate (0-63)
     * @return The algebraic notation string
     */
    public static String coordinateToAlgebraic(int coordinate) {
        if (coordinate < 0 || coordinate > 63) {
            return "";
        }
        
        int rank = coordinate / 8;
        int file = coordinate % 8;
        
        char fileChar = (char) ('a' + file);
        int rankNum = rank + 1;
        
        return fileChar + String.valueOf(rankNum);
    }
    
    /**
     * Converts algebraic notation to a board coordinate (e.g., a1 -> 0, h8 -> 63)
     * 
     * @param algebraic The algebraic notation string (e.g., "a1", "h8")
     * @return The board coordinate (0-63), or -1 if invalid
     */
    public static int algebraicToCoordinate(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            return -1;
        }
        
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);
        
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            return -1;
        }
        
        int file = fileChar - 'a';
        int rank = Character.getNumericValue(rankChar) - 1;
        
        return rank * 8 + file;
    }
} 