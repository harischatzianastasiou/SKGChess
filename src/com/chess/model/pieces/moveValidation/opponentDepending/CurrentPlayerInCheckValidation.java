package com.chess.model.pieces.moveValidation.opponentDepending;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.moves.Move;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Queen;
import com.chess.model.pieces.Rook;
import com.chess.model.pieces.moveValidation.MoveValidationStrategy;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;

public class CurrentPlayerInCheckValidation implements MoveValidationStrategy{

    @Override
    public boolean validate(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
        final Collection<Move> checkingMoves = CurrentPlayer.getOpponentCheckingMoves(boardTiles, opponentPlayer.getAlliance().getOpposite(), opponentPlayer);
        List<Integer> checkingPieceAttackPath = new ArrayList<>();
        if(!(piece instanceof King)){
            if(!checkingMoves.isEmpty()){
                if(checkingMoves.size() > 1){ //in double check only the king can move.
                    return false;
                }
                final Move checkingMove = checkingMoves.iterator().next();
                final Piece checkingPiece = checkingMove.getPieceToMove();
                final int kingCoordinate = checkingMove.getTargetCoordinate();
                
                // Add the checking piece's position to the attack path
                checkingPieceAttackPath.add(checkingPiece.getPieceCoordinate());
                // Add the rest of the attack path
                checkingPieceAttackPath.addAll(MoveValidationStrategy.calculateAttackPath(checkingPiece, kingCoordinate, boardTiles));
            
                if (!checkingPieceAttackPath.contains(candidateDestinationCoordinate)) { // if piece does not block the check or capture the checking piece then move is illegal
                    return false;
                }
            }
        }
        if(piece instanceof King){
            List<Integer> checkingPiecesThroughKingPath = new ArrayList<>();
            if(!checkingMoves.isEmpty()){
                for (Move checkingMove : checkingMoves) {
                    Piece checkingPiece = checkingMove.getPieceToMove();
                    int kingCoordinate = checkingMove.getTargetCoordinate();
                    if(checkingPiece instanceof Bishop || checkingPiece instanceof Queen || checkingPiece instanceof Rook){
                        int throughCoordinate = MoveValidationStrategy.getNextCoordinateInDirection(checkingPiece.getPieceCoordinate(), kingCoordinate);
                        if (CalculateMoveUtils.isCoordinateInBounds(throughCoordinate)) {
                            checkingPiecesThroughKingPath.add(throughCoordinate); // add the next coordinate that the attacking piece would target if king was not in the way of the attacking piece
                        }
                    }
                }
            }

            boolean isCandidateCoordinateInCheckingPieceThroughPath = checkingPiecesThroughKingPath.contains(candidateDestinationCoordinate);

            if (isCandidateCoordinateInCheckingPieceThroughPath) {
                return false;
            }
        }
    return true;
    }
}
