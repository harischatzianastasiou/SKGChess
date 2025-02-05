package com.chess.core.player.ai.engine.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.moves.capturing.CapturingMove;
import  com.chess.core.pieces.Piece;
import  com.chess.core.player.CurrentPlayer;
import  com.chess.core.player.ai.engine.evaluation.BoardEvaluator;


public class MiniMax {
    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private final Map<String, TranspositionEntry> transpositionTable;
    private static final int MAX_QUIESCENCE_DEPTH = 8; // Increased for better tactical vision
    private long nodesEvaluated;
    private static final int MATE_SCORE = 100000;
    private static final int TRANSPOSITION_TABLE_SIZE = 100000; // Reduced size but still effective
    private long startTime;
    private static final long MAX_SEARCH_TIME = 10000; // 10 seconds for better depth
    private static final int FUTILITY_MARGIN = 300; // Prune clearly bad moves
    private static final int[] MVV_LVA = {0, 1, 2, 3, 4, 5}; // Most Valuable Victim - Least Valuable Attacker
    private static final double HASH_TABLE_CLEANUP_THRESHOLD = 0.9; // Clean when 90% full
    
    private static class TranspositionEntry {
        int score;
        int depth;
        int flag; // 0: exact, 1: lower bound, 2: upper bound
        Move bestMove;
        long timestamp; // Add timestamp for aging
        
        TranspositionEntry(int score, int depth, int flag, Move bestMove) {
            this.score = score;
            this.depth = depth;
            this.flag = flag;
            this.bestMove = bestMove;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public MiniMax(BoardEvaluator evaluator, int searchDepth) {
        this.evaluator = evaluator;
        this.searchDepth = searchDepth;
        this.transpositionTable = Collections.synchronizedMap(new HashMap<>(TRANSPOSITION_TABLE_SIZE));
    }

    public Move findBestMove(IBoard board) {
        startTime = System.currentTimeMillis();
        nodesEvaluated = 0;
        
        // Clean old entries if table is getting full
        if (transpositionTable.size() >= TRANSPOSITION_TABLE_SIZE * HASH_TABLE_CLEANUP_THRESHOLD) {
            cleanTranspositionTable();
        }
        
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        Collection<Move> legalMoves = board.getCurrentPlayer().getMoves();
        if (legalMoves.isEmpty()) {
            return null;
        }
        
        List<Move> moves = new ArrayList<>(legalMoves);
        quickMoveOrder(moves);
        
        // Iterative deepening with time control
        for (int currentDepth = 1; currentDepth <= searchDepth; currentDepth++) {
            if (isTimeUp()) break;
            
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;
            
            for (final Move move : moves) {
                if (isTimeUp()) break;
                
                IBoard newBoard = move.execute();
                if (newBoard == null) continue; // Skip invalid moves
                
                int score = -alphaBeta(newBoard, currentDepth - 1, -beta, -alpha, 0);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, score);
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("Evaluated %,d nodes in %.2f seconds%n", 
            nodesEvaluated, executionTime / 1000.0);
        
        return bestMove != null ? bestMove : moves.get(0);
    }

    private boolean isTimeUp() {
        return System.currentTimeMillis() - startTime > MAX_SEARCH_TIME;
    }

    private int alphaBeta(final IBoard board, final int depth, int alpha, int beta, int qdepth) {
        if (isTimeUp()) return evaluator.evaluate(board, depth);
        
        nodesEvaluated++;
        String boardHash = board.getFEN();
        
        // Transposition table lookup
        TranspositionEntry entry = transpositionTable.get(boardHash);
        if (entry != null && entry.depth >= depth) {
            if (entry.flag == 0) return entry.score;
            if (entry.flag == 1) alpha = Math.max(alpha, entry.score);
            if (entry.flag == 2) beta = Math.min(beta, entry.score);
            if (alpha >= beta) return entry.score;
        }
        
        if (depth <= 0) {
            return qdepth >= MAX_QUIESCENCE_DEPTH ? 
                   evaluator.evaluate(board, depth) : 
                   quiescenceSearch(board, alpha, beta, qdepth);
        }
        
        if (isEndGameScenario(board)) {
            return evaluateEndgame(board, depth);
        }
        
        Collection<Move> legalMoves = board.getCurrentPlayer().getMoves();
        if (legalMoves.isEmpty()) {
            return evaluator.evaluate(board, depth);
        }
        
        List<Move> moves = new ArrayList<>(legalMoves);
        quickMoveOrder(moves);
        
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int originalAlpha = alpha;
        
        for (final Move move : moves) {
            if (isTimeUp()) break;
            
            IBoard newBoard = move.execute();
            if (newBoard == null) continue;
            
            int score = -alphaBeta(newBoard, depth - 1, -beta, -alpha, qdepth);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
                alpha = Math.max(alpha, score);
            }
            
            if (alpha >= beta) {
                break;
            }
        }
        
        // Only store if we have a valid result and space
        if (bestMove != null && transpositionTable.size() < TRANSPOSITION_TABLE_SIZE) {
            int flag = bestScore <= originalAlpha ? 2 : (bestScore >= beta ? 1 : 0);
            transpositionTable.put(boardHash, new TranspositionEntry(bestScore, depth, flag, bestMove));
        }
        
        return bestScore;
    }
    
    private int quiescenceSearch(final IBoard board, int alpha, int beta, int qdepth) {
        if (isTimeUp() || board == null) {
            return evaluator.evaluate(board, 0);
        }
        
        int standPat = evaluator.evaluate(board, 0);
        nodesEvaluated++;
        
        if (standPat >= beta || qdepth >= MAX_QUIESCENCE_DEPTH) {
            return standPat;
        }
        
        if (standPat > alpha) {
            alpha = standPat;
        }
        
        // Only consider captures for quiescence
        List<Move> tacticalMoves = getTacticalMoves(board);
        if (tacticalMoves.isEmpty()) {
            return standPat;
        }
        
        // Simple scoring for tactical moves
        quickMoveOrder(tacticalMoves);
        
        for (Move move : tacticalMoves) {
            if (isTimeUp()) break;
            
            try {
                IBoard newBoard = move.execute();
                if (newBoard != null) {
                    int score = -quiescenceSearch(newBoard, -beta, -alpha, qdepth + 1);
                    if (score >= beta) {
                        return beta;
                    }
                    if (score > alpha) {
                        alpha = score;
                    }
                }
            } catch (Exception e) {
                // Skip problematic moves but continue search
                continue;
            }
        }
        
        return alpha;
    }
    
    private List<Move> getTacticalMoves(IBoard board) {
        List<Move> tacticalMoves = new ArrayList<>();
        Collection<Move> legalMoves = board.getCurrentPlayer().getMoves();
        
        // Early exit if no moves
        if (legalMoves == null || legalMoves.isEmpty()) {
            return tacticalMoves;
        }
        
        // Single pass through moves
        for (Move move : legalMoves) {
            if (move instanceof CapturingMove) {
                tacticalMoves.add(move);
            }
        }
        return tacticalMoves;
    }
    
    private void quickMoveOrder(List<Move> moves) {
        if (moves == null || moves.size() <= 1) {
            return;
        }
        
        // Advanced move ordering:
        // 1. Captures (ordered by MVV-LVA)
        // 2. Checks
        // 3. Killer moves
        // 4. History moves
        Collections.sort(moves, (m1, m2) -> {
            if (m1 == null || m2 == null) return 0;
            
            // First prioritize captures
            boolean isCapture1 = m1 instanceof CapturingMove;
            boolean isCapture2 = m2 instanceof CapturingMove;
            
            if (isCapture1 && isCapture2) {
                // Use MVV-LVA for capture ordering
                CapturingMove c1 = (CapturingMove) m1;
                CapturingMove c2 = (CapturingMove) m2;
                int score1 = getMVVLVAScore(c1);
                int score2 = getMVVLVAScore(c2);
                return Integer.compare(score2, score1);
            }
            
            if (isCapture1) return -1;
            if (isCapture2) return 1;
            
            // Then prioritize checks
            boolean isCheck1 = moveCausesCheck(m1);
            boolean isCheck2 = moveCausesCheck(m2);
            
            if (isCheck1 && !isCheck2) return -1;
            if (!isCheck1 && isCheck2) return 1;
            
            return 0;
        });
    }

    private int getMVVLVAScore(CapturingMove move) {
        // Higher score for capturing valuable pieces with less valuable pieces
        int victimValue = getPieceValue(move.getCapturedPiece().getPieceSymbol());
        int attackerValue = getPieceValue(move.getPieceToMove().getPieceSymbol());
        return victimValue * 6 - attackerValue;
    }

    private boolean moveCausesCheck(Move move) {
        try {
            IBoard newBoard = move.execute();
            CurrentPlayer currentPlayer = (CurrentPlayer) newBoard.getCurrentPlayer();
            
            return newBoard != null && currentPlayer.isInCheck();
        } catch (Exception e) {
            return false;
        }
    }

    private int getPieceValue(Piece.PieceSymbol symbol) {
        switch (symbol) {
            case PAWN: return 1;
            case KNIGHT: return 2;
            case BISHOP: return 2;
            case ROOK: return 3;
            case QUEEN: return 4;
            case KING: return 5;
            default: return 0;
        }
    }
    
    private int evaluateEndgame(final IBoard board, final int depth) {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        if (currentPlayer.isCheckmate()) {
            return currentPlayer.getAlliance().isWhite() ? 
                -MATE_SCORE + depth : // Black wins

                MATE_SCORE - depth;  // White wins
        }
        return 0; // Stalemate
    }

    private static boolean isEndGameScenario(final IBoard board) {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        return currentPlayer.isCheckmate() || currentPlayer.isStalemate();
    }


    private void cleanTranspositionTable() {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - 30000; // Remove entries older than 30 seconds
        
        transpositionTable.entrySet().removeIf(entry -> 
            entry.getValue().timestamp < threshold);
    }
} 