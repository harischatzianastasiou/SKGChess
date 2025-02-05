package  com.chess.core.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.moves.noncapturing.KingSideCastleMove;
import  com.chess.core.moves.noncapturing.QueenSideCastleMove;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Piece;
import com.chess.core.tiles.Tile;
import com.chess.service.GameService;
import com.google.common.collect.ImmutableList;
import com.chess.core.player.Player;

public final class CurrentPlayer extends Player {
		
    private final boolean isInCheck;
    private final boolean isInCheckmate;
    private final boolean isCastled;
    
    private CurrentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance, boolean isInCheck, boolean isInCheckmate, boolean isCastled) {
        super(pieces, moves, alliance);
        this.isInCheck = isInCheck;
        this.isInCheckmate = isInCheckmate;
        this.isCastled = isCastled;
    }

	public static CurrentPlayer createCurrentPlayer(final List<Tile> tiles, final Alliance alliance,final Player opponentPlayer, final Move lastMove, final boolean isCastled) {
        final List<Piece> pieces = new ArrayList<>();
        final Collection<Move> moves = new ArrayList<>();
        final Collection<Move> opponentCheckingMoves = new ArrayList<>();
        boolean isInCheck = false;
        boolean isInCheckmate = false;
    

        // // Check if user has castled by looking for castle moves in game history
        // for (Move move : GameService.getGameById(GameService.getGameById()).getMoveHistory()) {
        //     if ((move instanceof KingSideCastleMove || move instanceof QueenSideCastleMove) && 
        //         move.getPieceToMove().getPieceAlliance() == alliance) {
        //         isCastled = true;
        //         break;
        //     }
        // }

        opponentCheckingMoves.addAll(getOpponentCheckingMoves(tiles, alliance, opponentPlayer));
        if(!opponentCheckingMoves.isEmpty()){
            isInCheck = true;
        }
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    pieces.add(piece);
                    moves.addAll(piece.calculateMoves(tiles, opponentPlayer, lastMove));
                }
            }
        }  

        if (isInCheck && moves.isEmpty()) {
            isInCheckmate = true; 
        }
        return new CurrentPlayer(ImmutableList.copyOf(pieces), ImmutableList.copyOf(moves), alliance, isInCheck, isInCheckmate, isCastled);    
    }

    public static Collection<Move> getOpponentCheckingMoves(final List<Tile> tiles, final Alliance alliance, final Player opponentPlayer) {// moves that are checking the current user's king
        Collection<Move> checkingMoves = new ArrayList<>();
        for (Move move : opponentPlayer.getMoves()) {
            if (move.getTargetCoordinate() == getKingCoordinate(tiles, alliance)) {
                checkingMoves.add(move);
            }
        }
        return checkingMoves;
    }

    public static int getKingCoordinate(final List<Tile> tiles, final Alliance alliance) {
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    if (piece instanceof King && piece.getPieceAlliance() == alliance) {
                        return piece.getPieceCoordinate();
                    }
                }
            }
        }
        throw new RuntimeException("No king found for this user");
    }

    public boolean isCheckmate() {
        return this.isInCheckmate;
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isStalemate() {
        return !this.isInCheck() && this.getMoves().isEmpty();
    }

    public boolean isCastled() {
        return this.isCastled;
    }
}