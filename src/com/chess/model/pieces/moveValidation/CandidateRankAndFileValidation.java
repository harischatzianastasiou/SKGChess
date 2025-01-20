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
        int rankDiff = sourceRank > destRank ? sourceRank - destRank : destRank - sourceRank;
        
        int sourceFile = CalculateMoveUtils.getCoordinateFile(piece.getPieceCoordinate());
        int destFile = CalculateMoveUtils.getCoordinateFile(candidateDestinationCoordinate);
        int fileDiff = sourceFile > destFile ? sourceFile - destFile : destFile - sourceFile;
        
        switch(piece.getPieceSymbol()){
            case ROOK:
            case QUEEN:
                // Must be moving along same rank or same file
                if((Math.abs(candidateOffset) == 1 || Math.abs(candidateOffset) == 8)){
                    if (!(rankDiff == 0 || fileDiff == 0)) {
                        return false;
                    }
                }
                
                // For horizontal movement
                if (Math.abs(candidateOffset) == 1) {
                    // Moving left (-1): can only move to columns left of current position
                    if (candidateOffset == -1 && destFile >= sourceFile) {
                        return false;
                    }
                    // Moving right (+1): can only move to columns right of current position
                    if (candidateOffset == 1 && destFile <= sourceFile) {
                        return false;
                    }
                }
                
                // For vertical movement
                if (Math.abs(candidateOffset) == 8) {
                    // Moving up (-8): can only move to ranks above current position
                    if (candidateOffset == -8 && destRank >= sourceRank) {
                        return false;
                    }
                    // Moving down (+8): can only move to ranks below current position
                    if (candidateOffset == 8 && destRank <= sourceRank) {
                        return false;
                    }
                }
                return true;
            case KING:
                return rankDiff <= 1 && fileDiff <= 1;
            default:
                return true;
        } 
    }
}
