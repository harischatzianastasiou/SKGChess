package com.chess.util;

import com.chess.core.moves.Move;
import com.chess.core.moves.capturing.CapturingMove;
import com.chess.core.moves.capturing.PawnEnPassantAttack;
import com.chess.core.moves.capturing.PawnPromotionCapturingMove;
import com.chess.core.moves.noncapturing.KingSideCastleMove;
import com.chess.core.moves.noncapturing.QueenSideCastleMove;
import com.chess.core.moves.noncapturing.PawnPromotionMove;
import com.chess.core.pieces.Piece;
import com.chess.core.pieces.Piece.PieceSymbol;

/**
 * Utility class for converting chess moves to algebraic notation.
 * Each method is simple and commented for learning.
 */
public class ChessNotationUtil {
    // Array for file letters (a-h)
    private static final String[] FILES = {"a", "b", "c", "d", "e", "f", "g", "h"};
    // Array for rank numbers (1-8)
    private static final String[] RANKS = {"1", "2", "3", "4", "5", "6", "7", "8"};

    /**
     * Convert a move to algebraic notation (e.g. e4, Nf3, O-O, bxc3, etc)
     * @param move The move object
     * @return The move in algebraic notation
     */
    public static String toAlgebraicNotation(Move move) {
        // Handle castling
        if (move instanceof KingSideCastleMove) return "O-O";
        if (move instanceof QueenSideCastleMove) return "O-O-O";

        // Get source and target squares
        String source = coordinateToSquare(move.getSourceCoordinate());
        String target = coordinateToSquare(move.getTargetCoordinate());
        Piece piece = move.getPieceToMove();
        PieceSymbol symbol = piece.getPieceSymbol();

        // Pawn moves
        if (symbol == PieceSymbol.PAWN) {
            // Pawn promotion
            if (move instanceof PawnPromotionMove) return target + "=Q";
            if (move instanceof PawnPromotionCapturingMove) return source.charAt(0) + "x" + target + "=Q";
            // En passant
            if (move instanceof PawnEnPassantAttack) return source.charAt(0) + "x" + target + " e.p.";
            // Pawn capture
            if (move instanceof CapturingMove) return source.charAt(0) + "x" + target;
            // Normal pawn move
            return target;
        }

        // Other pieces
        String pieceLetter = getPieceLetter(symbol);
        // Captures
        if (move instanceof CapturingMove) return pieceLetter + "x" + target;
        // Normal move
        return pieceLetter + target;
    }

    /**
     * Convert a board coordinate (0-63) to chess square (e.g. 0 -> a1, 63 -> h8)
     */
    public static String coordinateToSquare(int coordinate) {
        int file = coordinate % 8; // 0-7
        int rank = 7 - (coordinate / 8); // 0-7, inverted for chess notation
        return FILES[file] + RANKS[rank];
    }

    /**
     * Get the letter for a piece in algebraic notation
     */
    private static String getPieceLetter(PieceSymbol symbol) {
        switch (symbol) {
            case KNIGHT: return "N";
            case BISHOP: return "B";
            case ROOK:   return "R";
            case QUEEN:  return "Q";
            case KING:   return "K";
            default:     return ""; // Pawn has no letter
        }
    }
} 