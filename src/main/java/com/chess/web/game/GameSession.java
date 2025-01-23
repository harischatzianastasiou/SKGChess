package com.chess.web.game;

import java.time.Instant;

import com.chess.core.Alliance;
import com.chess.core.board.IBoard;
import com.chess.core.player.ai.engine.AIPlayer;
import com.chess.core.player.ai.engine.evaluation.StandardBoardEvaluator;
import com.chess.core.player.ai.engine.strategy.MinimaxStrategy;
import com.chess.util.GameHistory;

public class GameSession {
    private final String sessionId;
    private IBoard currentBoard;
    private final GameHistory gameHistory;
    private final AIPlayer aiPlayer;
    private Instant lastActivityTime;

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.currentBoard = IBoard.createStandardBoard();
        this.gameHistory = GameHistory.createNewInstance(); // Use factory method instead of constructor
        
        // Initialize AI player with black pieces
        MinimaxStrategy strategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
        this.aiPlayer = new AIPlayer(strategy, Alliance.BLACK);
        
        this.lastActivityTime = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public IBoard getCurrentBoard() {
        return currentBoard;
    }

    public void updateBoard(IBoard board) {
        this.currentBoard = board;
        this.gameHistory.addBoard(board);
        this.lastActivityTime = Instant.now();
    }

    public GameHistory getGameHistory() {
        return gameHistory;
    }

    public AIPlayer getAiPlayer() {
        return aiPlayer;
    }

    public Instant getLastActivityTime() {
        return lastActivityTime;
    }

    public void updateLastActivityTime() {
        this.lastActivityTime = Instant.now();
    }
} 