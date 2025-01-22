package com.chess.ai.utils;

import java.util.List;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Knight;
import com.chess.model.pieces.Pawn;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Queen;
import com.chess.model.pieces.Rook;

public class MoveConverter {
    
    // Map chess files (a-h) to board columns (0-7)
    private static final int[] FILE_MAP = {0, 1, 2, 3, 4, 5, 6, 7};  // a=0, b=1, etc.
    
    // Map chess ranks (1-8) to board rows (0-7), counting from top to bottom
    private static final int[] RANK_MAP = {0, 1, 2, 3, 4, 5, 6, 7};  // 8=0, 7=1, etc.
    
    public static Move fromLongAlgebraic(String moveStr, IBoard board) {
        if (moveStr == null || moveStr.length() < 4) {
            return null;
        }
        
        // Handle castling
        if (moveStr.equals("e1g1") || moveStr.equals("e8g8")) {
            return findMatchingMove(board, m -> m instanceof KingSideCastleMove);
        }
        if (moveStr.equals("e1c1") || moveStr.equals("e8c8")) {
            return findMatchingMove(board, m -> m instanceof QueenSideCastleMove);
        }
        
        // Parse source and target squares
        int sourceFile = moveStr.charAt(0) - 'a';
        int sourceRank = '8' - moveStr.charAt(1);
        int targetFile = moveStr.charAt(2) - 'a';
        int targetRank = '8' - moveStr.charAt(3);
        
        if (!isValidSquare(sourceFile, sourceRank) || !isValidSquare(targetFile, targetRank)) {
            return null;
        }
        
        int sourceCoord = sourceRank * 8 + sourceFile;
        int targetCoord = targetRank * 8 + targetFile;
        
        // Find matching move in legal moves
        return findMatchingMove(board, m -> 
            m.getSourceCoordinate() == sourceCoord && 
            m.getTargetCoordinate() == targetCoord);
    }
    
    public static String toLongAlgebraic(Move move) {
        if (move == null) {
            return null;
        }
        
        int sourceCoord = move.getSourceCoordinate();
        int targetCoord = move.getTargetCoordinate();
        
        char sourceFile = (char)('a' + (sourceCoord % 8));
        char sourceRank = (char)('8' - (sourceCoord / 8));
        char targetFile = (char)('a' + (targetCoord % 8));
        char targetRank = (char)('8' - (targetCoord / 8));
        
        return "" + sourceFile + sourceRank + targetFile + targetRank;
    }
    
    public static Move fromAlgebraic(String moveStr, IBoard board) {
        if (moveStr == null || moveStr.isEmpty()) {
            return null;
        }
        
        moveStr = moveStr.replaceAll("[+#!?]", ""); // Remove check/mate/annotation symbols
        
        // Handle castling
        if (moveStr.equals("O-O")) {
            return findMatchingMove(board, m -> m instanceof KingSideCastleMove);
        }
        if (moveStr.equals("O-O-O")) {
            return findMatchingMove(board, m -> m instanceof QueenSideCastleMove);
        }
        
        // Parse the move
        char pieceType = Character.isUpperCase(moveStr.charAt(0)) ? moveStr.charAt(0) : 'P';
        boolean isCapture = moveStr.contains("x");
        String square = moveStr.substring(moveStr.length() - 2); // Last two characters are always the target square
        
        // Parse target square
        int targetFile = square.charAt(0) - 'a';
        int targetRank = '8' - square.charAt(1);
        int targetCoord = targetRank * 8 + targetFile;
        
        // Find matching move in legal moves
        return findMatchingMove(board, m -> {
            Piece piece = m.getPieceToMove();
            boolean pieceMatches = piece instanceof Pawn ? pieceType == 'P' :
                                 piece instanceof Knight ? pieceType == 'N' :
                                 piece instanceof Bishop ? pieceType == 'B' :
                                 piece instanceof Rook ? pieceType == 'R' :
                                 piece instanceof Queen ? pieceType == 'Q' :
                                 piece instanceof King ? pieceType == 'K' : false;
            
            boolean captureMatches = isCapture == (m instanceof CapturingMove);
            boolean targetMatches = m.getTargetCoordinate() == targetCoord;
            
            return pieceMatches && captureMatches && targetMatches;
        });
    }
    
    private static boolean isValidSquare(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }
    
    private static Move findMatchingMove(IBoard board, java.util.function.Predicate<Move> predicate) {
        List<Move> legalMoves = (List<Move>) board.getCurrentPlayer().getMoves();
        for (Move move : legalMoves) {
            if (predicate.test(move)) {
                return move;
            }
        }
        return null;
    }
}
