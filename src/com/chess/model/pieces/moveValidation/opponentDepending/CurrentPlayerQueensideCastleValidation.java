package com.chess.model.pieces.moveValidation.opponentDepending;

import java.util.Collection;
import java.util.List;

import com.chess.model.moves.Move;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Rook;
import com.chess.model.pieces.moveValidation.MoveValidationStrategy;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class CurrentPlayerQueensideCastleValidation implements MoveValidationStrategy {

    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        boolean isCastlingPathSafe = false;
        if(opponentPlayer != null){
            final Collection<Move> checkingMoves = CurrentPlayer.getOpponentCheckingMoves(boardTiles, opponentPlayer.getAlliance().getOpposite(), opponentPlayer);
			if(checkingMoves.isEmpty()){
                if (piece.isFirstMove()) {
                    // Add queenside castle if rook is present
                    final Tile queenSideRookTile = boardTiles.get(piece.getPieceCoordinate() - 4);
                    if (queenSideRookTile.isTileOccupied() && queenSideRookTile.getPiece() instanceof Rook) {
                        Rook queenSideRook = (Rook) queenSideRookTile.getPiece();
                        if (queenSideRook.isFirstMove()) {
                            if (!boardTiles.get(piece.getPieceCoordinate() - 1).isTileOccupied() &&
                                !boardTiles.get(piece.getPieceCoordinate() - 2).isTileOccupied() &&
                                !boardTiles.get(piece.getPieceCoordinate() - 3).isTileOccupied()) {

                                // Check if castling path is under attack
                                int[] castlingPath = {piece.getPieceCoordinate(), piece.getPieceCoordinate() - 1, piece.getPieceCoordinate() - 2};
                                if( CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(piece.getPieceCoordinate())
                                || CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(piece.getPieceCoordinate() - 1)
                                || CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(piece.getPieceCoordinate() - 2)
                                || CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(piece.getPieceCoordinate() - 3)){
                                    return false;
                                }
                                isCastlingPathSafe = opponentPlayer.getMoves().stream()
                                    .noneMatch(move -> {
                                        if ((move.getTargetCoordinate() == piece.getPieceCoordinate()
                                            || move.getTargetCoordinate() == piece.getPieceCoordinate() - 1 
                                            || move.getTargetCoordinate() == piece.getPieceCoordinate() - 2
                                            || move.getTargetCoordinate() == piece.getPieceCoordinate() - 3)) {
                                            return true;
                                        }
                                        int targetSquare = move.getTargetCoordinate();
                                        for (int pathSquare : castlingPath) {
                                            if (targetSquare == pathSquare) return true;
                                        }
                                        return false;
                                    });
                            }
                        }
                    }
                }
            }
        }
        return isCastlingPathSafe;
    }
}
