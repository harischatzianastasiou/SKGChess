package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.pieces.Piece;

//memoization
public class BoardCache {
    private static final Map<String, Board> boardCache = new HashMap<>();

    public static Board getBoard(Board.Builder builder) {
        String boardKey = generateBoardKey(builder);
        return boardCache.computeIfAbsent(boardKey, k -> Board.createBoard(builder));
    }

    private static String generateBoardKey(Board.Builder builder) {
        // Generate a unique key based on the board configuration
        StringBuilder key = new StringBuilder();
        for (Map.Entry<Integer, Piece> entry : builder.getStartingPieces().entrySet()) {
            key.append(entry.getKey()).append(":").append(entry.getValue().toString()).append("|");
        }
        key.append(builder.getNextMovePlayer());
        return key.toString();
    }
}


//Useful for 

//1.
//Repetitive Positions:
//In some games, certain positions may repeat due to specific strategies or tactics, such as perpetual check or threefold repetition, where the same position occurs three times with the same player to move and the same set of possible moves.
//2.
//Analysis and AI:
//In chess engines and AI, board caching can be useful during analysis or when evaluating multiple potential moves. The engine might explore various branches of the game tree, some of which may lead back to previously evaluated positions.
//3.
//Performance Optimization:
//Caching can help optimize performance by avoiding redundant calculations. If a position has been evaluated before, the engine can reuse the results instead of recalculating legal moves, evaluating the board, etc.