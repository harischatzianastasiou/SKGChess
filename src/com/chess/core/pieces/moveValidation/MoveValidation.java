package  com.chess.core.pieces.moveValidation;

import java.util.List;

import  com.chess.core.pieces.Piece;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;

public class MoveValidation {
    private final List<MoveValidationStrategy> strategies;
    
    public MoveValidation(List<MoveValidationStrategy> strategies) {
        this.strategies = strategies;
    }
    
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        for (MoveValidationStrategy strategy : strategies) {
            if (!strategy.validate(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer)) {
                return false;
            }
        }
        return true;
    }
}
