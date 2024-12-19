package com.chess.model.pieces.moveValidation;

import java.util.List;

import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Piece;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class CandidateRankAndFileValidation implements MoveValidationStrategy {
    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        int rankDifference = CalculateMoveUtils.getCoordinateRankDifference(candidateDestinationCoordinate, piece.getPieceCoordinate());
        int fileDifference = CalculateMoveUtils.getCoordinateFileDifference(candidateDestinationCoordinate, piece.getPieceCoordinate());
        switch(piece.getPieceSymbol()){
            case ROOK:
                return rankDifference == 0 || fileDifference == 0;
            case KING:
                return rankDifference <= 1 && fileDifference <= 1;
            case QUEEN:
                if((Math.abs(candidateOffset) == 1 || Math.abs(candidateOffset) == 8)){
                    return rankDifference == 0 || fileDifference == 0;
                }
                break;
        } 
        return true;
    }
}
