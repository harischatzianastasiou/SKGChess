package com.chess.model.pieces;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.capturing.PawnPromotionCapturingMove;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.pieces.moveValidation.AllianceOfTileValidation;
import com.chess.model.pieces.moveValidation.CandidateRankAndFileValidation;
import com.chess.model.pieces.moveValidation.CoordinateInBoundsValidation;
import com.chess.model.pieces.moveValidation.MoveValidation;
import com.chess.model.pieces.moveValidation.MoveValidationStrategy;
import com.chess.model.pieces.moveValidation.opponentDepending.CurrentPlayerInCheckValidation;
import com.chess.model.pieces.moveValidation.opponentDepending.CurrentPlayerKingSafeSquaresValidation;
import com.chess.model.pieces.moveValidation.opponentDepending.CurrentPlayerKingsideCastleValidation;
import com.chess.model.pieces.moveValidation.opponentDepending.CurrentPlayerPiecePinnedValidation;
import com.chess.model.pieces.moveValidation.opponentDepending.CurrentPlayerQueensideCastleValidation;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;
import com.chess.util.GameHistory;
import com.google.common.collect.ImmutableList;

public final class CalculateMoveUtils {

    public static final int NUM_TILES = 64;
	public static final int NUM_TILES_PER_ROW = 8;
    public static final Collection<Move> NO_LEGAL_MOVES = ImmutableList.copyOf(new ArrayList<>());

    public static Collection<Move> calculate(List<Tile> boardTiles, final Piece piece, final int[] moveOffsets , final Player opponentPlayer){
        final Collection<Move> moves = new ArrayList<>();
        final Collection<Move> opponentMoves =  (opponentPlayer == null) ? null : opponentPlayer.getMoves();
        final int MAX_SQUARES = getMaxSquaresMoved(piece);

        for (final int candidateOffset : moveOffsets) {
            for(int squaresMoved=1; squaresMoved <= MAX_SQUARES; squaresMoved++ ) {
                int total_offset = candidateOffset * squaresMoved;
                final int candidateDestinationCoordinate = calculateDestinationCoordinate(piece, total_offset);

                List<MoveValidationStrategy> validations = new ArrayList<>(List.of(
                    new CoordinateInBoundsValidation(),
                    new AllianceOfTileValidation(),
                    new CandidateRankAndFileValidation()
                ));

                if (opponentPlayer != null) {
                    validations.add(new CurrentPlayerInCheckValidation());
                    validations.add(new CurrentPlayerPiecePinnedValidation());
                    validations.add(new CurrentPlayerKingSafeSquaresValidation());
                }

                MoveValidation moveValidation = new MoveValidation(validations);
                if (!moveValidation.validate(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer)) {
                    break;
                }

                final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);

                if(isCandidateTileEmpty(candidateDestinationTile)){
                   moves.addAll(addNonCapturingMoves(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer));
                }else{
                    if(isCandidateTileOccupiedByOpponent(piece,candidateDestinationTile))
                        moves.addAll(addCapturingMoves(piece, boardTiles,  candidateDestinationCoordinate, candidateOffset));
                    else
                        if(opponentPlayer == null)
                            ProtectedCoordinatesTracker.addProtectedCoordinate(candidateDestinationCoordinate); // Store the protected piece
                    break; //for sliding pieces if there is a piece in the direction that sliding piece can move, stop further checking in this direction.
                }
            }
        }
        return ImmutableList.copyOf(moves);
    }

    private static int calculateDestinationCoordinate(Piece piece, int total_offset) {
        if (piece instanceof Pawn) {
            Pawn pawn = (Pawn) piece;
            return piece.getPieceCoordinate() + (pawn.getAdvanceDirection() * total_offset);
        } else {
            return piece.getPieceCoordinate() + total_offset;
        }
    }

    public static Collection<Move> addNonCapturingMoves(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer){
        final List<Move> moves = new ArrayList<>();

        if(!(piece instanceof Pawn)){
            moves.add(new NonCapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
        }
        if(piece instanceof King && new CurrentPlayerKingsideCastleValidation().validate(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer) ){
            Tile kingSideRookTile = boardTiles.get(piece.getPieceCoordinate() + 3);
            Rook kingSideRook = (Rook) kingSideRookTile.getPiece();
            moves.add(new KingSideCastleMove(boardTiles, piece.getPieceCoordinate(), piece.getPieceCoordinate() + 2, piece, kingSideRook.getPieceCoordinate(), piece.getPieceCoordinate() + 1,kingSideRook));
        }

        if(piece instanceof King && new CurrentPlayerQueensideCastleValidation().validate(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer) ){
            Tile queenSideRookTile = boardTiles.get(piece.getPieceCoordinate()  - 4);
            Rook queenSideRook = (Rook) queenSideRookTile.getPiece();
            moves.add(new QueenSideCastleMove(boardTiles, piece.getPieceCoordinate(), piece.getPieceCoordinate() - 2, piece, queenSideRook.getPieceCoordinate(), piece.getPieceCoordinate() - 1, queenSideRook));
        }

        if(piece instanceof Pawn){
            Pawn pawn = (Pawn) piece;
            switch (Math.abs(candidateOffset)) {
                case 8:
                    if(pawn.getCurrentRank() != pawn.getPromotionRank()){
                        moves.add(new PawnMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                    } else {
                        moves.add(new PawnPromotionMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                    }
                    break;
                case 16:
                    if(pawn.getCurrentRank() == pawn.getInitialRank()){
                        int coordinateBeforeCandidate = piece.getPieceCoordinate() + ( 8 * pawn.getAdvanceDirection());
                        if(isCandidateTileEmpty(boardTiles.get(coordinateBeforeCandidate))){
                            moves.add(new PawnJumpMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                        }
                    }
                    break;
                case 7:
                case 9:
                    if(opponentPlayer == null)
                        ProtectedCoordinatesTracker.addProtectedCoordinate(candidateDestinationCoordinate);
                    if(pawn.getCurrentRank() == pawn.getEnPassantRank()){
                        Move lastMove = GameHistory.getInstance().getLastMove();
                        if (!(lastMove instanceof PawnJumpMove) || lastMove.getPieceToMove().getPieceAlliance() == piece.getPieceAlliance() || getCoordinateFile(lastMove.getTargetCoordinate()) != getCoordinateFile(candidateDestinationCoordinate)) {
                            return ImmutableList.copyOf(moves);
                        }
                        Piece pieceToCapture = boardTiles.get(lastMove.getTargetCoordinate()).getPiece();
                        moves.add(new PawnEnPassantAttack(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceToCapture));
                    }
                    break;
            }
        }
        return ImmutableList.copyOf(moves);
    }

    public static Collection<Move> addCapturingMoves( Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset){
        final List<Move> moves = new ArrayList<>();
        Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
        Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
        
        if(!(piece instanceof Pawn)){
            moves.add(new CapturingMove(boardTiles,piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceOnCandidateDestinationTile));
        }
        if(piece instanceof Pawn && (Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9)){
            Pawn pawn = (Pawn) piece;
            if(pawn.getCurrentRank() != pawn.getPromotionRank()){
                moves.add(new CapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceOnCandidateDestinationTile));
            }else
                moves.add(new PawnPromotionCapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceOnCandidateDestinationTile));
        }
        return ImmutableList.copyOf(moves);
    }

    public static boolean isCandidateTileEmpty(Tile candidateDestinationTile){
        return !candidateDestinationTile.isTileOccupied();
    }    

    public static boolean isCandidateTileOccupiedByOpponent(Piece piece, Tile candidateDestinationTile){
        Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
        final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
        return piece.getPieceAlliance() != allianceOfPieceOnCandidateDestinationTile;
    }

    public static boolean isCoordinateInBounds(final int tileCoordinate) {
        return tileCoordinate >= 0 && tileCoordinate < NUM_TILES;
    }

    public static int getCoordinateRank(int tileCoordinate) {
        return (Math.abs(tileCoordinate) / NUM_TILES_PER_ROW) + 1;
    }

    public static int getCoordinateFile(int tileCoordinate) {
        return (Math.abs(tileCoordinate) % NUM_TILES_PER_ROW) + 1;
    }

    public static int getCoordinateRankDifference(int destinationCoordinate, int sourceCoordinate) {
        return Math.abs(getCoordinateRank(destinationCoordinate)) - Math.abs(getCoordinateRank(sourceCoordinate));
    }

    public static int getCoordinateFileDifference(int destinationCoordinate, int sourceCoordinate) {
        return Math.abs(getCoordinateFile(destinationCoordinate)) - Math.abs(getCoordinateFile(sourceCoordinate));
    }

    public static Alliance getCoordinateAlliance(int tileCoordinate) {
        return (getCoordinateRank(tileCoordinate) + getCoordinateFile(tileCoordinate)) % 2 == 0 ? Alliance.BLACK : Alliance.WHITE;
    }

    public static int getMaxSquaresMoved(final Piece piece){
        switch(piece.getPieceSymbol()){
            case PAWN:
            case KING:
            case KNIGHT:
                return 1;
            case BISHOP:
            case ROOK:
            case QUEEN:
                return 7;
            default:
                return 0;
        }
    }

    public static class ProtectedCoordinatesTracker {
        private static final List<Integer> protectedCoordinates = new ArrayList<>();
    
        public static void addProtectedCoordinate(int coordinate) {
            protectedCoordinates.add(coordinate);
        }
    
        public static List<Integer> getProtectedCoordinates(){
            return ImmutableList.copyOf(protectedCoordinates);
        }
    
        public static void clear() {
            protectedCoordinates.clear(); // Clear the set for the next calculation
        }
    } 
}