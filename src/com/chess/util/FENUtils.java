package com.chess.util;

import  com.chess.core.board.IBoard;
import  com.chess.core.pieces.Piece;
import  com.chess.core.tiles.Tile;

public class FENUtils {
    
    public static String toFEN(IBoard board) {
        StringBuilder fen = new StringBuilder();
        
        // 1. Piece placement (from rank 8 to 1)
        for (int rank = 0; rank < 8; rank++) {
            int emptySquares = 0;
            
            for (int file = 0; file < 8; file++) {
                int coordinate = rank * 8 + file;
                Tile tile = board.getTile(coordinate);
                
                if (tile.isTileOccupied()) {
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    Piece piece = tile.getPiece();
                    String symbol = piece.getPieceSymbol().toString();
                    // Lowercase for black pieces, uppercase for white
                    String pieceChar = piece.getPieceAlliance().isWhite() ? symbol : symbol.toLowerCase();
                    fen.append(pieceChar);
                } else {
                    emptySquares++;
                }
            }
            
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            
            if (rank < 7) {
                fen.append('/');
            }
        }
        
        // 2. Active color
        fen.append(' ').append(board.getCurrentPlayer().getAlliance().isWhite() ? 'w' : 'b');
        
        // 3. Castling availability
        fen.append(" KQkq");
        
        // 4. En passant target square
        fen.append(" -");
        
        // 5. Halfmove clock
        fen.append(" 0");
        
        // 6. Fullmove number
        fen.append(" 1");
        
        return fen.toString();
    }

    public static boolean isValidFEN(String fen) {
        if (fen == null || fen.isEmpty()) {
            return false;
        }

        String[] parts = fen.split(" ");
        if (parts.length != 6) {
            return false;
        }

        // Validate piece placement
        String[] ranks = parts[0].split("/");
        if (ranks.length != 8) {
            return false;
        }

        // Validate active color
        if (!parts[1].equals("w") && !parts[1].equals("b")) {
            return false;
        }

        return true;
    }
} 