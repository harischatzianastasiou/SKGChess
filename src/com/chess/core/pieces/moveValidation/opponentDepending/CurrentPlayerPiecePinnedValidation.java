package  com.chess.core.pieces.moveValidation.opponentDepending;

import java.util.Collection;
import java.util.List;

import  com.chess.core.moves.Move;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.moveValidation.MoveValidationStrategy;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;

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
