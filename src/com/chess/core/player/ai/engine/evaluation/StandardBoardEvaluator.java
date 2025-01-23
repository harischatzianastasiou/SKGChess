package com.chess.core.player.ai.engine.evaluation;

import  com.chess.core.Alliance;
import  com.chess.core.board.IBoard;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Piece.PieceSymbol;
import  com.chess.core.player.CurrentPlayer;

public final class StandardBoardEvaluator implements BoardEvaluator {
    // Piece values (in centipawns)
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    
    // Strategic bonuses
    private static final int CHECK_BONUS = 50;
    private static final int CHECKMATE_BONUS = 10000;
    private static final int CASTLED_BONUS = 60;
    private static final int BISHOP_PAIR_BONUS = 50;
    private static final int MOBILITY_MULTIPLIER = 5;
    private static final int CENTER_CONTROL_BONUS = 10;
    private static final int PAWN_STRUCTURE_BONUS = 10;
    
    // Piece-Square tables
    private static final int[] PAWN_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };
    
    private static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };
    
    private static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };
    
    private static final int[] ROOK_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    };
    
    private static final int[] QUEEN_TABLE = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };
    
    private static final int[] KING_TABLE = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    };

    @Override
    public int evaluate(IBoard board, final int depth) {
        return calculateScore(board, Alliance.WHITE, depth) - 
               calculateScore(board, Alliance.BLACK, depth);
    }

    private int calculateScore(IBoard board, Alliance alliance, final int depth) {
        int score = 0;
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        
        // Material and position
        score += evaluateMaterial(board, alliance);
        score += evaluatePosition(board, alliance);
        
        // Mobility
        score += mobility(currentPlayer) * MOBILITY_MULTIPLIER;
        
        // King safety
        score += evaluateKingSafety(board, alliance);
        
        // Pawn structure
        score += evaluatePawnStructure(board, alliance);
        
        // Bishop pair
        score += evaluateBishopPair(board, alliance);
        
        // Tactical bonuses
        score += check(currentPlayer);
        score += checkmate(currentPlayer, depth);
        score += castled(currentPlayer);
        
        return score;
    }

    private int evaluateMaterial(IBoard board, Alliance alliance) {
        int score = 0;
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance) {
                score += getPieceValue(piece);
            }
        }
        return score;
    }

    private int evaluatePosition(IBoard board, Alliance alliance) {
        int score = 0;
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance) {
                int position = piece.getPieceCoordinate();
                if (alliance == Alliance.BLACK) {
                    position = 63 - position; // Flip for black
                }
                score += getPiecePositionBonus(piece.getPieceSymbol(), position);
            }
        }
        return score;
    }

    private int getPiecePositionBonus(PieceSymbol pieceType, int position) {
        switch (pieceType) {
            case PAWN: return PAWN_TABLE[position];
            case KNIGHT: return KNIGHT_TABLE[position];
            case BISHOP: return BISHOP_TABLE[position];
            case ROOK: return ROOK_TABLE[position];
            case QUEEN: return QUEEN_TABLE[position];
            case KING: return KING_TABLE[position];
            default: return 0;
        }
    }

    private int evaluateKingSafety(IBoard board, Alliance alliance) {
        int score = 0;
        Piece king = findKing(board, alliance);
        if (king == null) return 0;
        
        int kingPos = king.getPieceCoordinate();
        int rank = kingPos / 8;
        int file = kingPos % 8;
        
        // Penalize exposed king
        if (isKingExposed(board, kingPos, alliance)) {
            score -= 50;
        }
        
        // Bonus for pawn shield
        score += countPawnShield(board, kingPos, alliance) * 10;
        
        return score;
    }

    private int evaluatePawnStructure(IBoard board, Alliance alliance) {
        int score = 0;
        
        // Evaluate each file
        for (int file = 0; file < 8; file++) {
            int pawnsOnFile = countPawnsOnFile(board, file, alliance);
            if (pawnsOnFile > 1) {
                score -= 20; // Doubled pawns penalty
            }
            if (pawnsOnFile == 0 && hasOpenFile(board, file)) {
                score -= 10; // Open file penalty
            }
        }
        
        // Evaluate isolated and backward pawns
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance && piece.getPieceSymbol() == PieceSymbol.PAWN) {
                if (isIsolatedPawn(board, piece.getPieceCoordinate(), alliance)) {
                    score -= 15;
                }
                if (isBackwardPawn(board, piece.getPieceCoordinate(), alliance)) {
                    score -= 10;
                }
            }
        }
        
        return score;
    }

    private int evaluateBishopPair(IBoard board, Alliance alliance) {
        int bishops = 0;
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance && 
                piece.getPieceSymbol() == PieceSymbol.BISHOP) {
                bishops++;
            }
        }
        return bishops >= 2 ? BISHOP_PAIR_BONUS : 0;
    }

    private static int mobility(final CurrentPlayer currentPlayer) {
        return currentPlayer.getMoves().size() * MOBILITY_MULTIPLIER;
    }

    private static int check(final CurrentPlayer currentPlayer) {
        return currentPlayer.isInCheck() ? CHECK_BONUS : 0;
    }

    private static int checkmate(final CurrentPlayer currentPlayer, final int depth) {
        return currentPlayer.isCheckmate() ? CHECKMATE_BONUS : 0;
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
            case KING: return 0;
            default: throw new IllegalStateException("Unknown piece type: " + piece.getPieceSymbol());
        }
    }

    // Helper methods
    private Piece findKing(IBoard board, Alliance alliance) {
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance && 
                piece.getPieceSymbol() == PieceSymbol.KING) {
                return piece;
            }
        }
        return null;
    }

    private boolean isKingExposed(IBoard board, int kingPos, Alliance alliance) {
        int rank = kingPos / 8;
        return (alliance == Alliance.WHITE && rank < 6) || 
               (alliance == Alliance.BLACK && rank > 1);
    }

    private int countPawnShield(IBoard board, int kingPos, Alliance alliance) {
        int shield = 0;
        int rank = kingPos / 8;
        int file = kingPos % 8;
        
        // Check three files around king
        for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
            if (hasPawnShieldPiece(board, f, rank, alliance)) {
                shield++;
            }
        }
        return shield;
    }

    private boolean hasPawnShieldPiece(IBoard board, int file, int rank, Alliance alliance) {
        int pawnRank = alliance == Alliance.WHITE ? rank - 1 : rank + 1;
        if (pawnRank < 0 || pawnRank > 7) return false;
        
        int pos = pawnRank * 8 + file;
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceCoordinate() == pos && 
                piece.getPieceAlliance() == alliance &&
                piece.getPieceSymbol() == PieceSymbol.PAWN) {
                return true;
            }
        }
        return false;
    }

    private int countPawnsOnFile(IBoard board, int file, Alliance alliance) {
        int count = 0;
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceAlliance() == alliance && 
                piece.getPieceSymbol() == PieceSymbol.PAWN &&
                piece.getPieceCoordinate() % 8 == file) {
                count++;
            }
        }
        return count;
    }

    private boolean hasOpenFile(IBoard board, int file) {
        return countPawnsOnFile(board, file, Alliance.WHITE) == 0 &&
               countPawnsOnFile(board, file, Alliance.BLACK) == 0;
    }

    private boolean isIsolatedPawn(IBoard board, int pawnPos, Alliance alliance) {
        int file = pawnPos % 8;
        return (file == 0 || countPawnsOnFile(board, file - 1, alliance) == 0) &&
               (file == 7 || countPawnsOnFile(board, file + 1, alliance) == 0);
    }

    private boolean isBackwardPawn(IBoard board, int pawnPos, Alliance alliance) {
        int file = pawnPos % 8;
        int rank = pawnPos / 8;
        
        // Check if pawn can be supported by friendly pawns
        int leftFile = file - 1;
        int rightFile = file + 1;
        
        if (leftFile >= 0 && canPawnSupport(board, leftFile, rank, alliance)) return false;
        if (rightFile <= 7 && canPawnSupport(board, rightFile, rank, alliance)) return false;
        
        return true;
    }

    private boolean canPawnSupport(IBoard board, int file, int rank, Alliance alliance) {
        int supportRank = alliance == Alliance.WHITE ? rank + 1 : rank - 1;
        if (supportRank < 0 || supportRank > 7) return false;
        
        int pos = supportRank * 8 + file;
        for (Piece piece : board.getAllPieces()) {
            if (piece.getPieceCoordinate() == pos && 
                piece.getPieceAlliance() == alliance &&
                piece.getPieceSymbol() == PieceSymbol.PAWN) {
                return true;
            }
        }
        return false;
    }
} 