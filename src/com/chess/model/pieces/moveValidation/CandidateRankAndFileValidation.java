package com.chess.model.pieces.moveValidation;

import java.util.List;

import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Piece;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class CandidateRankAndFileValidation implements MoveValidationStrategy {
    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        int sourceRank = CalculateMoveUtils.getCoordinateRank(piece.getPieceCoordinate());
        int destRank = CalculateMoveUtils.getCoordinateRank(candidateDestinationCoordinate);
        int rankDiff = Math.max(sourceRank, destRank) - Math.min(sourceRank, destRank);
        
        int sourceFile = CalculateMoveUtils.getCoordinateFile(piece.getPieceCoordinate());
        int destFile = CalculateMoveUtils.getCoordinateFile(candidateDestinationCoordinate);
        int fileDiff = Math.max(destFile, sourceFile) - Math.min(destFile, sourceFile);
        
        // Prevent wrapping around board edges
        if (Math.abs(sourceFile - destFile) > 4) { // If file difference is more than half the board, it's wrapping
            return false;
        }
        
        switch(piece.getPieceSymbol()){
            case ROOK:
                return rankDiff == 0 || fileDiff == 0;
            case KING:
                return rankDiff <= 1 && fileDiff <= 1;
            case QUEEN:
                if((Math.abs(candidateOffset) == 1 || Math.abs(candidateOffset) == 8)){
                    return rankDiff == 0 || fileDiff == 0;
                }
                break;
        } 
        return true;
    }
}
