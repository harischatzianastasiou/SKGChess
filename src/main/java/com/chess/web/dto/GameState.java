package com.chess.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.core.pieces.Piece;
import com.chess.core.tiles.Tile;

public class GameState {
    private final String sessionId;
    private final List<PieceDTO> pieces;
    private final Move lastMove;
    private final boolean isGameOver;
    private final String gameResult;

    public GameState(String sessionId, IBoard board, Move lastMove, boolean isGameOver, String gameResult) {
        this.sessionId = sessionId;
        this.pieces = convertBoardToPieces(board);
        this.lastMove = lastMove;
        this.isGameOver = isGameOver;
        this.gameResult = gameResult;
    }

    private List<PieceDTO> convertBoardToPieces(IBoard board) {
        List<PieceDTO> pieces = new ArrayList<>(64);
        for (int i = 0; i < 64; i++) {
            Tile tile = board.getTile(i);
            Piece piece = tile.getPiece();
            if (piece != null) {
                pieces.add(new PieceDTO(piece.getPieceAlliance().toString(), piece.getClass().getSimpleName().toUpperCase()));
            } else {
                pieces.add(null);
            }
        }
        return pieces;
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public List<PieceDTO> getPieces() { return pieces; }
    public Move getLastMove() { return lastMove; }
    public boolean isGameOver() { return isGameOver; }
    public String getGameResult() { return gameResult; }

    // Inner class for piece representation
    public static class PieceDTO {
        private final String alliance;
        private final String type;

        public PieceDTO(String alliance, String type) {
            this.alliance = alliance;
            this.type = type;
        }

        public String getAlliance() { return alliance; }
        public String getType() { return type; }
    }
} 