package  com.chess.core.pieces.moveValidation;

import java.util.List;

import  com.chess.core.pieces.CalculateMoveUtils;
import  com.chess.core.pieces.Piece;
import  com.chess.core.player.Player;
import  com.chess.core.tiles.Tile;

public class CoordinateInBoundsValidation implements MoveValidationStrategy {
    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        return candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < CalculateMoveUtils.NUM_TILES;
    }
}
