package com.chess.engine.board;

import com.chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveResult {
    private static final MoveResult DEFAULT_INSTANCE = new MoveResult(
        null, new ArrayList<>(), new HashMap<>(), new HashMap<>()
    );

    private final Board board;
    private final List<Piece> checkingPieces;
    private final Map<Piece, Piece> pinnedPieces;
    private final Map<Piece, Piece> protectedPieces;

    private MoveResult(Board board, List<Piece> checkingPieces, Map<Piece, Piece> pinnedPieces, Map<Piece, Piece> protectedPieces) {
        this.board = board;
    	this.checkingPieces = checkingPieces;
        this.pinnedPieces = pinnedPieces;
        this.protectedPieces = protectedPieces;
    }
    
    private MoveResult() {
    	this.board = null;
    	this.checkingPieces = new ArrayList<>();
    	this.pinnedPieces = new HashMap<>();
    	this.protectedPieces = new HashMap<>();
    }

    public static MoveResult getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public List<Piece> getCheckingPieces() {
        return checkingPieces;
    }

    public Map<Piece, Piece> getPinnedPieces() {
        return pinnedPieces;
    }

    public Map<Piece, Piece> getProtectedPieces() {
        return protectedPieces;
    }

    public static MoveResult create(Board simulatedBoard) {
        return new MoveResult(
            simulatedBoard,
            calculateCheckingPieces(simulatedBoard),
            calculatePinnedPieces(simulatedBoard),
            calculateProtectedPieces(simulatedBoard)
        );
    }

    private static List<Piece> calculateCheckingPieces(Board board) {
        // Implementation for calculating checking pieces
        return new ArrayList<>();
    }

    private static Map<Piece, Piece> calculatePinnedPieces(Board board) {
        // Implementation for calculating pinned pieces
        return new HashMap<>();
    }

    private static Map<Piece, Piece> calculateProtectedPieces(Board board) {
        // Implementation for calculating protected pieces
        return new HashMap<>();
    }
}