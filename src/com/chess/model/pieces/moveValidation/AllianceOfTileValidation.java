package com.chess.model.pieces.moveValidation;

import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.pieces.Piece;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class AllianceOfTileValidation implements MoveValidationStrategy {
    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
        final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
        final Tile currentTile = boardTiles.get(piece.getPieceCoordinate());
        final Alliance allianceOfCurrentTile = currentTile.getTileAlliance();

        switch(piece.getPieceSymbol()){
            case BISHOP:
                return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
            case QUEEN:
                if(Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9){
                    return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                }
                break;
            case KNIGHT:
                return allianceOfCandidateDestinationTile != allianceOfCurrentTile;
            case PAWN:
                if(Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9){
                    return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                }
                break;
        }
        return true;
    }
}
