package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.capturing.PawnPromotionCapturingMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.pieces.Piece.PieceSymbol;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;
import com.chess.util.GameHistory;
import com.google.common.collect.ImmutableList;

public final class 1 {

    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_PER_ROW = 8;
    public static final Collection<Move> NO_LEGAL_MOVES = ImmutableList.copyOf(new ArrayList<>());

    public static Collection<Move> calculate(List<Tile> boardTiles, final Piece piece, final int[] moveOffsets, final Player opponentPlayer) {
        final Collection<Move> moves = new ArrayList<>();
        final Collection<Move> opponentMoves = (opponentPlayer == null) ? null : opponentPlayer.getMoves();
        final Collection<Move> checkingMoves = (opponentPlayer == null) ? null : CurrentPlayer.getOpponentCheckingMoves(boardTiles, opponentPlayer.getOppositeAlliance(), opponentPlayer);

        // Initialize strategies
        MoveStrategy moveStrategy = MoveStrategyFactory.getMoveStrategy(piece);
        ValidationStrategy validationStrategy = new CompositeValidationStrategy(opponentMoves);

        for (final int candidateOffset : moveOffsets) {
            for (int squaresMoved = 1; squaresMoved <= getMaxSquaresMoved(piece); squaresMoved++) {
                int total_offset = candidateOffset * squaresMoved;
                final int candidateDestinationCoordinate = calculateDestinationCoordinate(piece, total_offset);

                if (!isCoordinateInBounds(candidateDestinationCoordinate)) {
                    break;
                }

                final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
                moves.addAll(moveStrategy.addMoves(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer));

                // Validate the move
                if (!validationStrategy.isValidMove(piece, candidateDestinationCoordinate, candidateOffset, boardTiles)) {
                    break;
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

    // Strategy interface for moves
    interface MoveStrategy {
        Collection<Move> addMoves(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer);
    }

    // Factory for creating move strategies
    static class MoveStrategyFactory {
        public static MoveStrategy getMoveStrategy(Piece piece) {
            if (piece instanceof Pawn) {
                return new PawnMoveStrategy();
            } else {
                return new GenericMoveStrategy();
            }
        }
    }

    // Concrete strategy for non-capturing moves
    static class GenericMoveStrategy implements MoveStrategy {
        @Override
        public Collection<Move> addMoves(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
            List<Move> moves = new ArrayList<>();
            Tile candidateTile = boardTiles.get(candidateDestinationCoordinate);
            if (TileCheckStrategy.isCandidateTileEmpty(candidateTile)) {
                moves.add(new NonCapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
            } else if (TileCheckStrategy.isCandidateTileOccupiedByOpponent(piece, candidateTile)) {
                moves.add(new CapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, candidateTile.getPiece()));
            }
            return moves;
        }
    }

    // Concrete strategy for pawn moves
    static class PawnMoveStrategy implements MoveStrategy {
        @Override
        public Collection<Move> addMoves(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer) {
            List<Move> moves = new ArrayList<>();
            Pawn pawn = (Pawn) piece;
            switch (Math.abs(candidateOffset)) {
                case 8:
                    if (pawn.getCurrentRank() != pawn.getPromotionRank()) {
                        moves.add(new PawnMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                    } else {
                        moves.add(new PawnPromotionMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                    }
                    break;
                case 16:
                    if (pawn.getCurrentRank() == pawn.getInitialRank()) {
                        int coordinateBeforeCandidate = piece.getPieceCoordinate() + (8 * pawn.getAdvanceDirection());
                        if (TileCheckStrategy.isCandidateTileEmpty(boardTiles.get(coordinateBeforeCandidate))) {
                            moves.add(new PawnJumpMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                        }
                    }
                    break;
                case 7:
                case 9:
                    if (opponentPlayer == null) {
                        ProtectedCoordinatesTracker.addProtectedCoordinate(candidateDestinationCoordinate);
                    }
                    // Handle en passant logic...
                    break;
            }
            return moves;
        }
    }

    // Strategy interface for validation
    interface ValidationStrategy {
        boolean isValidMove(Piece piece, int candidateDestinationCoordinate, int candidateOffset, List<Tile> boardTiles);
    }

    // Composite validation strategy
    static class CompositeValidationStrategy implements ValidationStrategy {
        private final Collection<Move> opponentMoves;

        public CompositeValidationStrategy(Collection<Move> opponentMoves) {
            this.opponentMoves = opponentMoves;
        }

        @Override
        public boolean isValidMove(Piece piece, int candidateDestinationCoordinate, int candidateOffset, List<Tile> boardTiles) {
            return new TileAllianceValidationStrategy().isValidMove(piece, candidateDestinationCoordinate, candidateOffset, boardTiles) &&
                   new RankFileValidationStrategy().isValidMove(piece, candidateDestinationCoordinate, candidateOffset, boardTiles)
        }
    }

    // Concrete strategy for alliance validation
    static class TileAllianceValidationStrategy implements ValidationStrategy {
        @Override
        public boolean isValidMove(Piece piece, int candidateDestinationCoordinate, int candidateOffset, List<Tile> boardTiles) {
            final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
            final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
            final Tile currentTile = boardTiles.get(piece.getPieceCoordinate());
            final Alliance allianceOfCurrentTile = currentTile.getTileAlliance();

            switch (piece.getPieceSymbol()) {
                case BISHOP:
                    return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                case QUEEN:
                    if (Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9) {
                        return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                    }
                    break;
                case KNIGHT:
                    return allianceOfCandidateDestinationTile != allianceOfCurrentTile;
                case PAWN:
                    if (Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9) {
                        return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                    }
                    break;
            }
            return true;
        }
    }

    // Concrete strategy for rank validation
    static class RankFileValidationStrategy implements ValidationStrategy {
        @Override
        public boolean isValidMove(Piece piece, int candidateDestinationCoordinate, int candidateOffset, List<Tile> boardTiles) {
            int rankDifference = getCoordinateRankDifference(candidateDestinationCoordinate, piece.getPieceCoordinate());
            int fileDifference = getCoordinateFileDifference(candidateDestinationCoordinate, piece.getPieceCoordinate());
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

    // Tile Check Strategy
    static class TileCheckStrategy {
        public static boolean isCandidateTileEmpty(Tile candidateDestinationTile) {
            return !candidateDestinationTile.isTileOccupied();
        }

        public static boolean isCandidateTileOccupiedByOpponent(Piece piece, Tile candidateDestinationTile) {
            Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
            final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
            return piece.getPieceAlliance() != allianceOfPieceOnCandidateDestinationTile;
        }
    }

    public static boolean isCoordinateInBounds(final int tileCoordinate) {
        return tileCoordinate >= 0 && tileCoordinate < NUM_TILES;
    }

    public static int getMaxSquaresMoved(final Piece piece) {
        switch (piece.getPieceSymbol()) {
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
}
