package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.List;

import com.chess.model.tiles.Tile;

public final class CalculateMoveUtils {
    
    private CalculateMoveUtils() {
        // Utility class - prevent instantiation
    }

    public static List<Integer> calculateAttackPath(final Piece checkingPiece, 
                                                  final int kingCoordinate, 
                                                  final List<Tile> boardTiles) {
        List<Integer> pathCoordinates = new ArrayList<>();
        
        if (checkingPiece == null || boardTiles == null) {
            return pathCoordinates;
        }
        
        final int checkingPieceCoordinate = checkingPiece.getPieceCoordinate();
        
        switch(checkingPiece.getPieceSymbol()) {
            case BISHOP:
                return calculateDiagonalAttackPath(checkingPieceCoordinate, kingCoordinate);
                
            case ROOK:
                return calculateStraightAttackPath(checkingPieceCoordinate, kingCoordinate);
                
            case QUEEN:
                if (isOnDiagonal(checkingPieceCoordinate, kingCoordinate)) {
                    return calculateDiagonalAttackPath(checkingPieceCoordinate, kingCoordinate);
                } else {
                    return calculateStraightAttackPath(checkingPieceCoordinate, kingCoordinate);
                }
                
            case KNIGHT:
                pathCoordinates.add(checkingPieceCoordinate);
                return pathCoordinates;
                
            case PAWN:
                pathCoordinates.add(checkingPieceCoordinate);
                return pathCoordinates;
                
            default:
                return pathCoordinates;
        }
    }

    public static List<Integer> calculateDiagonalAttackPath(final int startCoordinate, final int endCoordinate) {
        List<Integer> pathCoordinates = new ArrayList<>();
        pathCoordinates.add(startCoordinate);
        
        int offset;
        if (Math.abs(endCoordinate - startCoordinate) % 7 == 0) {
            offset = 7 * Integer.signum(endCoordinate - startCoordinate);
        } else if (Math.abs(endCoordinate - startCoordinate) % 9 == 0) {
            offset = 9 * Integer.signum(endCoordinate - startCoordinate);
        } else {
            return pathCoordinates;
        }
        
        int currentCoordinate = startCoordinate + offset;
        while (currentCoordinate != endCoordinate) {
            pathCoordinates.add(currentCoordinate);
            currentCoordinate += offset;
        }
        
        return pathCoordinates;
    }

    public static List<Integer> calculateStraightAttackPath(final int startCoordinate, final int endCoordinate) {
        List<Integer> pathCoordinates = new ArrayList<>();
        pathCoordinates.add(startCoordinate);
        
        int offset;
        if (Math.abs(endCoordinate - startCoordinate) >= 8) {
            offset = 8 * Integer.signum(endCoordinate - startCoordinate);
        } else {
            offset = Integer.signum(endCoordinate - startCoordinate);
        }
        
        int currentCoordinate = startCoordinate + offset;
        while (currentCoordinate != endCoordinate) {
            pathCoordinates.add(currentCoordinate);
            currentCoordinate += offset;
        }
        
        return pathCoordinates;
    }

    public static boolean isOnDiagonal(final int startCoordinate, final int endCoordinate) {
        int diff = Math.abs(endCoordinate - startCoordinate);
        return diff % 7 == 0 || diff % 9 == 0;
    }

    public static int getNextCoordinateInDirection(final int startCoordinate, final int throughCoordinate) {
        int diff = throughCoordinate - startCoordinate;
        
        if (Math.abs(diff) % 7 == 0) {
            return throughCoordinate + (7 * Integer.signum(diff));
        } else if (Math.abs(diff) % 9 == 0) {
            return throughCoordinate + (9 * Integer.signum(diff));
        } else if (Math.abs(diff) % 8 == 0) {
            return throughCoordinate + (8 * Integer.signum(diff));
        } else {
            return throughCoordinate + Integer.signum(diff);
        }
    }
} 