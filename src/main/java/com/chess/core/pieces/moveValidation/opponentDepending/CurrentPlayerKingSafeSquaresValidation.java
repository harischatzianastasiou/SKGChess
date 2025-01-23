package  com.chess.core.pieces.moveValidation.opponentDepending;

import java.util.Collection;
import java.util.List;

import  com.chess.core.moves.Move;
import  com.chess.core.moves.noncapturing.PawnJumpMove;
import  com.chess.core.moves.noncapturing.PawnMove;
import  com.chess.core.moves.noncapturing.PawnPromotionMove;
import  com.chess.core.pieces.CalculateMoveUtils.ProtectedCoordinatesTracker;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.moveValidation.MoveValidationStrategy;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;

public class CurrentPlayerKingSafeSquaresValidation implements MoveValidationStrategy{
    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        if(piece instanceof King){
            final Collection<Move> opponentMoves = opponentPlayer.getMoves();
            boolean isCandidateCoordinateUnderAttack = opponentMoves.stream()
            .anyMatch(move -> {
                // If it's a pawn's forward move, ignore it (pawns can't attack(capture) forward)
                if (move instanceof PawnMove || move instanceof PawnPromotionMove || move instanceof PawnJumpMove) {
                    return false;
                }
                // For all other moves, check if they target the square
                return move.getTargetCoordinate() == candidateDestinationCoordinate;
            });

            boolean isCandidateCoordinateProtectedByOpponentPiece = ProtectedCoordinatesTracker.getProtectedCoordinates().contains(candidateDestinationCoordinate);

            if (isCandidateCoordinateUnderAttack || isCandidateCoordinateProtectedByOpponentPiece ) {
                return false;
            }
        }
        return true;
    }
}
