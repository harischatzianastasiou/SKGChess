package com.chess.model.pieces.moveValidation.opponentDepending;

import java.util.Collection;
import java.util.List;

import com.chess.model.moves.Move;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.pieces.CalculateMoveUtils.ProtectedCoordinatesTracker;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.moveValidation.MoveValidationStrategy;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

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
