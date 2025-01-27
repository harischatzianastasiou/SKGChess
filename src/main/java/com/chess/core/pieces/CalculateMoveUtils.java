package  com.chess.core.pieces;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import  com.chess.application.game.GameService;
import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.moves.capturing.CapturingMove;
import  com.chess.core.moves.capturing.PawnEnPassantAttack;
import  com.chess.core.moves.capturing.PawnPromotionCapturingMove;
import  com.chess.core.moves.noncapturing.KingSideCastleMove;
import  com.chess.core.moves.noncapturing.NonCapturingMove;
import  com.chess.core.moves.noncapturing.PawnJumpMove;
import  com.chess.core.moves.noncapturing.PawnMove;
import  com.chess.core.moves.noncapturing.PawnPromotionMove;
import  com.chess.core.moves.noncapturing.QueenSideCastleMove;
import  com.chess.core.pieces.moveValidation.AllianceOfTileValidation;
import  com.chess.core.pieces.moveValidation.CandidateRankAndFileValidation;
import  com.chess.core.pieces.moveValidation.CoordinateInBoundsValidation;
import  com.chess.core.pieces.moveValidation.MoveValidation;
import  com.chess.core.pieces.moveValidation.MoveValidationStrategy;
import  com.chess.core.pieces.moveValidation.opponentDepending.CurrentPlayerInCheckValidation;
import  com.chess.core.pieces.moveValidation.opponentDepending.CurrentPlayerKingSafeSquaresValidation;
import  com.chess.core.pieces.moveValidation.opponentDepending.CurrentPlayerKingsideCastleValidation;
import  com.chess.core.pieces.moveValidation.opponentDepending.CurrentPlayerPiecePinnedValidation;
import  com.chess.core.pieces.moveValidation.opponentDepending.CurrentPlayerQueensideCastleValidation;
import  com.chess.core.player.Player;
import com.chess.core.tiles.Tile;
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

                List<MoveValidationStrategy> basicValidations = new ArrayList<>(List.of(
                    new CoordinateInBoundsValidation(),
                    new AllianceOfTileValidation(),
                    new CandidateRankAndFileValidation()
                ));

                // Basic validations that should break the direction if they fail
                MoveValidation basicValidation = new MoveValidation(basicValidations);
                if (!basicValidation.validate(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer)) {
                    break;
                }

                // Check and pin validations that should break the direction only if there is a piece in the destination tile
                // Else continue checking in the direction
                if (opponentPlayer != null) {
                    List<MoveValidationStrategy> checkValidations = new ArrayList<>(List.of(
                        new CurrentPlayerInCheckValidation(),
                        new CurrentPlayerPiecePinnedValidation(),
                        new CurrentPlayerKingSafeSquaresValidation()
                    ));
                    
                    final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
                    MoveValidation checkValidation = new MoveValidation(checkValidations);
                    if (!checkValidation.validate(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer)) {
                        if (!isCandidateTileEmpty(candidateDestinationTile)) {
                            break;  // Break only if we hit a piece that fails validation
                        }
                        continue;  // Otherwise just skip this square and continue checking
                    }
                }

                final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);

                if(isCandidateTileEmpty(candidateDestinationTile)){
                   moves.addAll(addNonCapturingMoves(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer));
                }else{
                    if(isCandidateTileOccupiedByOpponent(piece,candidateDestinationTile))
                        moves.addAll(addCapturingMoves(piece, boardTiles,  candidateDestinationCoordinate, candidateOffset));
                    else {
                        if(opponentPlayer == null){
                            ProtectedCoordinatesTracker.addProtectedCoordinate(candidateDestinationCoordinate); // Store the protected piece
                        }
                    }
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
                        System.out.println("GameService.getCurrentGameId() : " + GameService.getCurrentGameId());
                        Move lastMove = GameService.getGame(GameService.getCurrentGameId()).getLastMove();
                        System.out.println("lastMove : " + lastMove.getSourceCoordinate() + " " + lastMove.getTargetCoordinate());
                        System.out.println("lastMove instanceof PawnJumpMove : " + (lastMove instanceof PawnJumpMove));
                        System.out.println("lastMove.getPieceToMove().getPieceAlliance() : " + lastMove.getPieceToMove().getPieceAlliance());
                        System.out.println("piece.getPieceAlliance() : " + piece.getPieceAlliance());
                        System.out.println("getCoordinateFile(lastMove.getTargetCoordinate()) : " + getCoordinateFile(lastMove.getTargetCoordinate()));
                        System.out.println("getCoordinateFile(candidateDestinationCoordinate) : " + getCoordinateFile(candidateDestinationCoordinate));
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