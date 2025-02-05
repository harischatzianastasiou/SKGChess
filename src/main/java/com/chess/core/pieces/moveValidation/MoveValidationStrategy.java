package  com.chess.core.pieces.moveValidation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import  com.chess.core.moves.Move;
import  com.chess.core.pieces.CalculateMoveUtils;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Piece.PieceSymbol;
import  com.chess.core.player.CurrentPlayer;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;
import com.google.common.collect.ImmutableList;

public interface MoveValidationStrategy {
    public abstract boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer);

    public static List<Integer> calculateAttackPath(final Piece checkingPiece, final int kingCoordinate, final List<Tile> boardTiles) {
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
        pathCoordinates.add(endCoordinate);
        
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
        pathCoordinates.add(endCoordinate);
        
        return pathCoordinates;
    }

    public static boolean isOnDiagonal(final int startCoordinate, final int endCoordinate) {
        int diff = Math.abs(endCoordinate - startCoordinate);
        if((CalculateMoveUtils.getCoordinateRank(endCoordinate) == 1 && CalculateMoveUtils.getCoordinateRank(startCoordinate) == 8)
        || CalculateMoveUtils.getCoordinateRank(endCoordinate) == 8 && CalculateMoveUtils.getCoordinateRank(startCoordinate) == 1){
            if(diff % 7 ==0 || diff % 9 == 0) return false;
        }
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

    public static List<Integer> calculateAttackPathOfPinningPiece(Piece piece, List<Tile> boardTiles, Collection<Move> oppositeuserMoves){
        int numOfPinningPieces = 0;
        final List<Integer> pinningPieceAttackPath = new ArrayList<>();// will be used in pinning
        final int kingCoordinate = CurrentPlayer.getKingCoordinate(boardTiles, piece.getPieceAlliance());     
        List<Move> movesPinningPiece= oppositeuserMoves.stream()
            .filter(move -> move.getTargetCoordinate() == piece.getPieceCoordinate())
            .collect(Collectors.toList());
            
        for(Move move : movesPinningPiece) {
            if(move.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN 
            || move.getPieceToMove().getPieceSymbol() == PieceSymbol.KING
            || move.getPieceToMove().getPieceSymbol() == PieceSymbol.KNIGHT){
                continue; // Pawns, Kings and Knights cannot pin other pieces
            }
            Piece pinningPiece= move.getPieceToMove();
            int throughCoordinate =  piece.getPieceCoordinate();
            // Keep looking through coordinates until we hit a piece or board edge
            while(true) {
                //must check alliance of throughTile and currentPieceTile
                throughCoordinate = getNextCoordinateInDirection( pinningPiece.getPieceCoordinate(), throughCoordinate);
            
                if (!CalculateMoveUtils.isCoordinateInBounds(throughCoordinate)) {
                    break;  // Stop if we hit board edge
                }
                
                Tile throughTile = boardTiles.get(throughCoordinate);
                
                if (throughTile.isTileOccupied()) {
                    Piece pieceInPath = throughTile.getPiece();
                    // If it's our king, this pawn is pinned - can only capture the attacking piece
                    if (pieceInPath.getPieceSymbol() == PieceSymbol.KING && 
                        pieceInPath.getPieceAlliance() == piece.getPieceAlliance()) {
                        pinningPieceAttackPath.addAll(calculateAttackPath(pinningPiece, kingCoordinate, boardTiles));
                        numOfPinningPieces++;    
                    }
                    break;  // Stop when we hit any piece
                }
            }
        }
        if(numOfPinningPieces > 1){
            return null;
        }
        return ImmutableList.copyOf(pinningPieceAttackPath);
    }
}
