package com.chess.web.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.core.GameController;
import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.core.pieces.Piece;
import com.chess.core.tiles.Tile;
import com.chess.web.dto.GameState;
import com.chess.web.dto.MoveRequest;
import com.chess.web.game.GameSession;
import com.chess.web.game.HeadlessChessBoard;

import jakarta.annotation.PreDestroy;

@RestController
@RequestMapping("/api/chess")
public class ChessGameController {
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30); // Timeout after 30 minutes of inactivity
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ChessGameController() {
        // Schedule cleanup task to run every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 5, 5, TimeUnit.MINUTES);
    }

    private void cleanupInactiveSessions() {
        Instant now = Instant.now();
        activeSessions.entrySet().removeIf(entry -> {
            GameSession session = entry.getValue();
            Duration inactiveDuration = Duration.between(session.getLastActivityTime(), now);
            return inactiveDuration.compareTo(SESSION_TIMEOUT) > 0;
        });
    }

    @PostMapping("/new-game")
    public ResponseEntity<GameState> newGame() {
        String sessionId = UUID.randomUUID().toString();
        GameSession session = new GameSession(sessionId);
        activeSessions.put(sessionId, session);

        return ResponseEntity.ok(new GameState(
            sessionId,
            session.getCurrentBoard(),
            null,
            false,
            null
        ));
    }

    @PostMapping("/{sessionId}/move")
    public ResponseEntity<GameState> makeMove(
            @PathVariable String sessionId,
            @RequestBody MoveRequest moveRequest) {

        // Validate session
        GameSession session = activeSessions.get(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        // Validate move request
        if (!isValidMoveRequest(moveRequest)) {
            return ResponseEntity.badRequest()
                .body(new GameState(
                    sessionId,
                    session.getCurrentBoard(),
                    null,
                    false,
                    "Invalid move coordinates"
                ));
        }

        try {
            // Create headless board for move execution
            HeadlessChessBoard headlessBoard = new HeadlessChessBoard(session.getCurrentBoard());
            
            // Execute player's move
            GameController gameController = new GameController(session.getCurrentBoard());
            IBoard newBoard = executeMove(gameController, session, moveRequest, headlessBoard);
            
            if (newBoard == null) {
                return ResponseEntity.badRequest()
                    .body(new GameState(
                        sessionId,
                        session.getCurrentBoard(),
                        null,
                        false,
                        "Invalid move - not allowed by chess rules"
                    ));
            }

            // Check for game end conditions
            boolean isGameOver = gameController.isCheckmate() ;
            // || gameController.isDraw();
            String gameResult = null;
            if (isGameOver) {
                if (gameController.isCheckmate()) {
                    gameResult = "Checkmate! " + newBoard.getOpponentPlayer().getAlliance() + " wins!";
                } else {
                    gameResult = "Draw!";
                }
            }

            // Let AI respond if game is not over
            Move aiMove = null;
            if (!isGameOver) {
                try {
                    aiMove = session.getAiPlayer().makeMove(newBoard);
                    headlessBoard.updateBoard(newBoard);
                    newBoard = gameController.executeAIMove(aiMove, headlessBoard);
                    
                    // Check again for game end after AI move
                    if (gameController.isCheckmate()) {
                        isGameOver = true;
                        gameResult = "Checkmate! " + newBoard.getOpponentPlayer().getAlliance() + " wins!";
                    }
                        // } else if (gameController.isDraw()) {
                    //     isGameOver = true;
                    //     gameResult = "Draw!";
                    // }
                } catch (Exception e) {
                    // Log AI error but continue with the game
                    e.printStackTrace();
                    return ResponseEntity.ok(new GameState(
                        sessionId,
                        newBoard,
                        null,
                        false,
                        "AI player encountered an error. Your move was successful."
                    ));
                }
            }

            session.updateBoard(newBoard);
            session.updateLastActivityTime();
            
            return ResponseEntity.ok(new GameState(
                sessionId,
                newBoard,
                aiMove,
                isGameOver,
                gameResult
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new GameState(
                    sessionId,
                    session.getCurrentBoard(),
                    null,
                    false,
                    "An error occurred while processing your move"
                ));
        }
    }

    private boolean isValidMoveRequest(MoveRequest moveRequest) {
        // Validate coordinates are within board bounds (0-63)
        return moveRequest != null &&
               moveRequest.getSourceCoordinate() >= 0 && 
               moveRequest.getSourceCoordinate() < 64 &&
               moveRequest.getTargetCoordinate() >= 0 && 
               moveRequest.getTargetCoordinate() < 64;
    }

    private IBoard executeMove(GameController gameController, GameSession session, MoveRequest moveRequest, HeadlessChessBoard headlessBoard) {
        // Select source and target tiles
        headlessBoard.selectTile(moveRequest.getSourceCoordinate());
        headlessBoard.selectTile(moveRequest.getTargetCoordinate());
        
        // Execute the move
        return gameController.executeMove(headlessBoard);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<GameState> getGameState(@PathVariable String sessionId) {
        GameSession session = activeSessions.get(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        session.updateLastActivityTime();
        return ResponseEntity.ok(new GameState(
            sessionId,
            session.getCurrentBoard(),
            null,
            false,
            null
        ));
    }

    @GetMapping("/{sessionId}/legal-moves/{coordinate}")
    public ResponseEntity<List<Integer>> getLegalMoves(
            @PathVariable String sessionId,
            @PathVariable int coordinate) {

        // Validate session
        GameSession session = activeSessions.get(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        // Get the piece at the coordinate
        IBoard board = session.getCurrentBoard();
        Tile tile = board.getTile(coordinate);
        if (!tile.isTileOccupied()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // Get legal moves for the piece
        Piece piece = tile.getPiece();
        Collection<Move> moves = piece.calculateMoves(board.getTiles(), board.getOpponentPlayer());
        
        // Convert moves to target coordinates
        List<Integer> legalMoveCoordinates = moves.stream()
            .map(Move::getTargetCoordinate)
            .collect(Collectors.toList());

        return ResponseEntity.ok(legalMoveCoordinates);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 