package com.chess.core.utils;

import com.chess.core.Alliance;
import com.chess.core.pieces.Piece;
import com.chess.core.pieces.Piece.PieceSymbol;

/**
 * Utility class to calculate piece values for material advantage tracking
 * Standard chess piece values:
 * - Pawn: 1 point
 * - Knight: 3 points  
 * - Bishop: 3 points
 * - Rook: 5 points
 * - Queen: 9 points
 * - King: 0 points (not counted as it cannot be captured)
 */
public class PieceValueCalculator {
    
    // Standard chess piece values
    private static final int PAWN_VALUE = 1;
    private static final int KNIGHT_VALUE = 3;
    private static final int BISHOP_VALUE = 3;
    private static final int ROOK_VALUE = 5;
    private static final int QUEEN_VALUE = 9;
    private static final int KING_VALUE = 0; // King is not counted as it cannot be captured
    
    /**
     * Get the point value of a piece based on its symbol
     * @param pieceSymbol The symbol of the piece
     * @return The point value of the piece
     */
    public static int getPieceValue(PieceSymbol pieceSymbol) {
        return switch (pieceSymbol) {
            case PAWN -> PAWN_VALUE;
            case KNIGHT -> KNIGHT_VALUE;
            case BISHOP -> BISHOP_VALUE;
            case ROOK -> ROOK_VALUE;
            case QUEEN -> QUEEN_VALUE;
            case KING -> KING_VALUE;
        };
    }
    
    /**
     * Calculate the total material value for a specific alliance on the board
     * @param pieces Collection of all pieces on the board
     * @param alliance The alliance (WHITE or BLACK) to calculate for
     * @return Total point value of pieces for the given alliance
     */
    public static int calculateMaterialValue(Iterable<Piece> pieces, Alliance alliance) {
        int totalValue = 0;
        
        // Iterate through all pieces and sum values for the specified alliance
        for (Piece piece : pieces) {
            if (piece.getPieceAlliance() == alliance) {
                totalValue += getPieceValue(piece.getPieceSymbol());
            }
        }
        
        return totalValue;
    }
    
    /**
     * Calculate the material advantage for a player
     * Positive value means the player has more material
     * Negative value means the player has less material
     * Zero means equal material
     * 
     * @param pieces Collection of all pieces on the board
     * @param playerAlliance The alliance of the player to calculate advantage for
     * @return Material advantage from the player's perspective
     */
    public static int calculateMaterialAdvantage(Iterable<Piece> pieces, Alliance playerAlliance) {
        // Calculate material for both alliances
        int playerMaterial = calculateMaterialValue(pieces, playerAlliance);
        int opponentMaterial = calculateMaterialValue(pieces, playerAlliance.getOpposite());
        
        // Return the difference (positive if player has more, negative if less)
        return playerMaterial - opponentMaterial;
    }
}
