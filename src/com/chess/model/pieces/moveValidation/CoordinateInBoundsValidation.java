package com.chess.model.pieces.moveValidation;

import java.util.List;

import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Piece;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class CoordinateInBoundsValidation implements MoveValidationStrategy {
    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        return candidateDestinationCoordinate >= 0 && candidateDestinationCoordinate < CalculateMoveUtils.NUM_TILES;
    }
}
