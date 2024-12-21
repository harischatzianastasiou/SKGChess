package com.chess.model.pieces.moveValidation.opponentDepending;

import java.util.Collection;
import java.util.List;

import com.chess.model.moves.Move;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.moveValidation.MoveValidationStrategy;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class CurrentPlayerPiecePinnedValidation implements MoveValidationStrategy {

    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        final Collection<Move> opponentMoves =  (opponentPlayer == null) ? null : opponentPlayer.getMoves();
        List<Integer> pinningPieceAttackPath = MoveValidationStrategy.calculateAttackPathOfPinningPiece(piece, boardTiles, opponentMoves);
        if(!(piece instanceof King)){
            if(pinningPieceAttackPath == null){
                return false;
            } 

            if(!pinningPieceAttackPath.isEmpty() && !pinningPieceAttackPath.contains(candidateDestinationCoordinate)){
                return false;
            }
        }
        return true;
    }
}
