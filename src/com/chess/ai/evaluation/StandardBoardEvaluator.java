package com.chess.ai.evaluation;

import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.Piece;
import com.chess.model.player.CurrentPlayer;

public final class StandardBoardEvaluator implements BoardEvaluator {
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 300;
    private static final int BISHOP_VALUE = 300;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int CHECK_BONUS = 50;
    private static final int CHECKMATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 100;
    private static final int CASTLED_BONUS = 60;

    @Override
    public int evaluate(IBoard board, final int depth) {
        int whiteScore = calculateScore(board, Alliance.WHITE, depth);
        int blackScore = calculateScore(board, Alliance.BLACK, depth);
        return whiteScore - blackScore;
    }

    private int calculateScore(IBoard board, Alliance alliance, final int depth) {
        int score = 0;
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance) {
                score += getPieceValue(piece);
            }
        }
        return score + 
            mobility(currentPlayer) +
            check(currentPlayer) +
            checkmate(currentPlayer, depth) +
            castled(currentPlayer);
    }

    private static int mobility(final CurrentPlayer currentPlayer) {
        return currentPlayer.getMoves().size();
    }

    private static int check(final CurrentPlayer currentPlayer) {
        return currentPlayer.isInCheck() ? CHECK_BONUS  : 0;
    }

    private static int checkmate(final CurrentPlayer currentPlayer, final int depth) {
        return currentPlayer.isCheckmate() ? CHECKMATE_BONUS * depthBonus(depth) : 0;
    }

    private static int depthBonus(final int depth) {
        return depth == 0 ? 1 : depth * DEPTH_BONUS;
    }

    private static int castled(final CurrentPlayer currentPlayer) {
        return currentPlayer.isCastled() ? CASTLED_BONUS : 0;
    }


    private static int getPieceValue(Piece piece) {
        switch (piece.getPieceSymbol()) {
            case PAWN: return PAWN_VALUE;
            case KNIGHT: return KNIGHT_VALUE;
            case BISHOP: return BISHOP_VALUE;
            case ROOK: return ROOK_VALUE;
            case QUEEN: return QUEEN_VALUE;
            case KING: return 0; // King's value is not considered in material evaluation
            default: throw new IllegalStateException("Unknown piece type: " + piece.getPieceSymbol());
        }
    }
} 