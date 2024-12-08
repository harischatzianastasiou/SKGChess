package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.board.BoardUtils;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.pieces.Piece.PieceSymbol;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.tiles.Tile;

public final class CalculateMoveUtils {
    
    private static final int MAX_SQUARES_MOVED = 7;

    public enum MovementType {
        STRAIGHT,    // Rook moves
        DIAGONAL     // Bishop moves
    }

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

    /**
     * Calculates moves for sliding pieces (Queen, Rook, Bishop)
     */
    public static List<Move> calculateSlidingPieceMoves(
            final List<Tile> boardTiles,
            final Piece piece,
            final int[] moveOffsets,
            final MovementType movementType,
            final Collection<Move> checkingMoves,
            final Collection<Move> oppositePlayerMoves,
            final boolean validateCheck) {

        final List<Move> legalMoves = new ArrayList<>();
        
        if (validateCheck) {
            // Handle check validation
            List<Integer> checkingPieceAttackPath = new ArrayList<>();
            List<Integer> pinningPiecesCoordinates = new ArrayList<>();
            
            if (checkingMoves.size() > 1) {
                return legalMoves; // In double check, only king can move
            }
            
            if (checkingMoves.size() == 1) {
                final Move checkingMove = checkingMoves.iterator().next();
                final Piece checkingPiece = checkingMove.getPieceToMove();
                final int kingCoordinate = checkingMove.getTargetCoordinate();
                checkingPieceAttackPath.addAll(calculateAttackPath(checkingPiece, kingCoordinate, boardTiles));
            } else {
                // Check for pins
                pinningPiecesCoordinates = calculatePinningPieces(boardTiles, piece, oppositePlayerMoves);
                if (pinningPiecesCoordinates.size() > 1) {
                    return legalMoves; // In double pin, only king can move
                }
            }

            // Calculate actual moves with pin/check validation
            return calculateValidatedMoves(boardTiles, piece, moveOffsets, movementType, 
                    checkingPieceAttackPath, pinningPiecesCoordinates);
        } else {
            // Calculate moves without check validation (for opponent's moves)
            return calculateValidatedMoves(boardTiles, piece, moveOffsets, movementType, 
                    new ArrayList<>(), new ArrayList<>());
        }
    }

    private static List<Integer> calculatePinningPieces(
            final List<Tile> boardTiles,
            final Piece piece,
            final Collection<Move> oppositePlayerMoves) {
        
        List<Integer> pinningPiecesCoordinates = new ArrayList<>();
        final int kingPosition = CurrentPlayer.getKingCoordinate(boardTiles, piece.getPieceAlliance());
        
        List<Move> movesTargetingPiece = oppositePlayerMoves.stream()
            .filter(move -> move.getTargetCoordinate() == piece.getPieceCoordinate())
            .collect(Collectors.toList());
            
        for (Move targetingMove : movesTargetingPiece) {
            Piece potentialPinningPiece = targetingMove.getPieceToMove();
            if (!canPiecePin(potentialPinningPiece)) {
                continue;
            }
            
            if (isPiecePinned(boardTiles, piece, potentialPinningPiece, kingPosition)) {
                pinningPiecesCoordinates.add(potentialPinningPiece.getPieceCoordinate());
            }
        }
        
        return pinningPiecesCoordinates;
    }

    private static boolean canPiecePin(Piece piece) {
        return piece.getPieceSymbol() != PieceSymbol.PAWN && 
               piece.getPieceSymbol() != PieceSymbol.KING &&
               piece.getPieceSymbol() != PieceSymbol.KNIGHT;
    }

    private static boolean isPiecePinned(
            final List<Tile> boardTiles,
            final Piece pieceToCheck,
            final Piece potentialPinningPiece,
            final int kingPosition) {
        
        int throughCoordinate = pieceToCheck.getPieceCoordinate();
        
        while (true) {
            throughCoordinate = getNextCoordinateInDirection(
                potentialPinningPiece.getPieceCoordinate(),
                throughCoordinate
            );
            
            if (!BoardUtils.isValidTileCoordinate(throughCoordinate)) {
                break;
            }
            
            Tile throughTile = boardTiles.get(throughCoordinate);
            if (throughTile.isTileOccupied()) {
                Piece pieceInPath = throughTile.getPiece();
                if (pieceInPath.getPieceSymbol() == PieceSymbol.KING && 
                    pieceInPath.getPieceAlliance() == pieceToCheck.getPieceAlliance()) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private static List<Move> calculateValidatedMoves(
            final List<Tile> boardTiles,
            final Piece piece,
            final int[] moveOffsets,
            final MovementType movementType,
            final List<Integer> checkingPieceAttackPath,
            final List<Integer> pinningPiecesCoordinates) {
        
        final List<Move> legalMoves = new ArrayList<>();
        
        directionLoop:for (final int offset : moveOffsets) {
            // Check each direction (offset)
             for (int squaresMoved = 1; squaresMoved <= MAX_SQUARES_MOVED; squaresMoved++) {
                final int destinationCoordinate = piece.getPieceCoordinate() + (offset * squaresMoved);
                
                if (!BoardUtils.isValidTileCoordinate(destinationCoordinate)) {
                    break; // Break out of this direction entirely
                }

                // If we're in check, only allow moves that block the check or capture the checking piece
                if (!checkingPieceAttackPath.isEmpty()) {
                    if (!checkingPieceAttackPath.contains(destinationCoordinate)) {
                        continue; // Skip moves that don't block the check
                    }
                } else if (!pinningPiecesCoordinates.isEmpty()) {
                    // If piece is pinned, it can only move along the pin line
                    if (destinationCoordinate != pinningPiecesCoordinates.get(0)) {
                        continue;
                    }
                }

                if (!isValidDirection(piece.getPieceCoordinate(), destinationCoordinate, movementType)) {
                    break; // Break out of this direction entirely
                }

                final Tile destinationTile = boardTiles.get(destinationCoordinate);
                
                if (!destinationTile.isTileOccupied()) {
                    legalMoves.add(new NonCapturingMove(boardTiles, piece.getPieceCoordinate(), 
                            destinationCoordinate, piece));
                } else {
                    final Piece pieceAtDestination = destinationTile.getPiece();
                    if (piece.getPieceAlliance() != pieceAtDestination.getPieceAlliance()) {
                        legalMoves.add(new CapturingMove(boardTiles, piece.getPieceCoordinate(),
                                destinationCoordinate, piece, pieceAtDestination));
                    }
                    break; // Break out of this direction entirely when we hit any piece
                }
            }
        }
        
        return legalMoves;
    }

    private static boolean isValidDirection(final int sourceCoordinate, 
                                         final int targetCoordinate,
                                         final MovementType movementType) {
        int rankDifference = BoardUtils.getCoordinateRankDifference(targetCoordinate, sourceCoordinate);
        int fileDifference = BoardUtils.getCoordinateFileDifference(targetCoordinate, sourceCoordinate);

        switch(movementType) {
            case STRAIGHT:
                return rankDifference == 0 || fileDifference == 0;
            case DIAGONAL:
                return Math.abs(rankDifference) == Math.abs(fileDifference);
            default:
                return false;
        }
    }

    public static int getMaxSquaresMoved(final Piece piece){
        if(piece instanceof Pawn)
            return 1;
        if(piece instanceof King)
            return 1;
        if(piece instanceof Knight)
            return 1;
        if(piece instanceof Bishop || piece instanceof Rook || piece instanceof Queen)
            return 7;
        return 0;
    }
}