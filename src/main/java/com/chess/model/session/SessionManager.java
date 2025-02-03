package com.chess.model.session;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;


import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chess.service.PlayerService;
import com.chess.model.entity.Player;
import com.chess.repository.GameRepository;
import lombok.Data;

@Component
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final PlayerService playerService;
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();
    private final PriorityQueue<QueuedPlayer> matchmakingQueue = new PriorityQueue<>();
    private final Map<String, String> sessionToPlayerId = new ConcurrentHashMap<>();
    private boolean gameCreated = false; // flag to check if a game has been created. needs careful handling.
    
    public SessionManager(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Data
    public class QueuedPlayer implements Comparable<QueuedPlayer> {
        private final String username;
        private final String sessionId;
        private final LocalDateTime joinedAt;

        @Override
        public int compareTo(QueuedPlayer other) {
            return this.joinedAt.compareTo(other.joinedAt);
        }
    }
    
    public void createSession(String sessionId, Object session) {
        logger.info("Creating session - sessionId: {}", sessionId);
        activeSessions.add(sessionId);
    }

    public void removeSession(String sessionId) {
        logger.info("Removing session - sessionId: {}", sessionId);
        activeSessions.remove(sessionId);
        sessionToPlayerId.remove(sessionId);
        removeFromMatchmakingQueue(sessionId);
    }

    public void addToMatchmakingQueue(String sessionId, String username) {
        logger.info("Adding to matchmaking queue - sessionId: {}, username: {}", sessionId, username);
        if (isSessionActive(sessionId)) {
            // Find player by username and store their ID
            Player player = playerService.getPlayerByUsername(username);
            sessionToPlayerId.put(sessionId, player.getId().toString());
            
            matchmakingQueue.offer(new QueuedPlayer(username, sessionId, LocalDateTime.now()));
            tryMatchPlayers();
        } else {
            logger.warn("Attempted to add inactive session to queue - sessionId: {}", sessionId);
        }
    }

    public boolean isSessionActive(String sessionId) {
        return activeSessions.contains(sessionId);
    }

    public void removeFromMatchmakingQueue(String sessionId) {
        logger.info("Removing from matchmaking queue - sessionId: {}", sessionId);
        matchmakingQueue.removeIf(qp -> qp.getSessionId().equals(sessionId));
    }

    private synchronized boolean tryMatchPlayers() {
        logger.info("Trying to match players - current queue size: {}", getQueueSize());
        if (getQueueSize() >= 2) {
            QueuedPlayer player1 = pollQueue();
            QueuedPlayer player2 = pollQueue();
            
            if (player1 != null && player2 != null) {
                logger.info("Found match - player1: {}, player2: {}", player1.getUsername(), player2.getUsername());
                
                // Remove players from queue first to prevent duplicate matches
                removeFromMatchmakingQueue(player1.getSessionId());
                removeFromMatchmakingQueue(player2.getSessionId());
                
                // Randomly assign colors
                if (Math.random() < 0.5) {
                    QueuedPlayer temp = player1;
                    player1 = player2;
                    player2 = temp;
                }
                
                String player1Id = sessionToPlayerId.get(player1.getSessionId());
                String player2Id = sessionToPlayerId.get(player2.getSessionId());
                
                if(player1Id != null && player2Id != null) {
                    this.gameCreated = true;
                }
            }
        }
        return this.gameCreated;
    }

    public boolean isPlayerInQueue(String sessionId) {
        return matchmakingQueue.stream()
                .anyMatch(qp -> qp.getSessionId().equals(sessionId));
    }

    public void addPlayerToSession(String sessionId, String playerId) {
        logger.info("Adding player to session - playerId: {}, sessionId: {}", playerId, sessionId);
        sessionToPlayerId.put(sessionId, playerId);
        activeSessions.add(sessionId);
    }

    public void removePlayerFromSession(String sessionId) {
        sessionToPlayerId.remove(sessionId);
    }

    public QueuedPlayer pollQueue() {
        return matchmakingQueue.poll();
    }

    public int getQueueSize() {
        return matchmakingQueue.size();
    }

    public String getPlayerIdFromSession(String sessionId) {
        return sessionToPlayerId.get(sessionId);
    }

    public boolean isGameCreated() {
        return gameCreated;
    }

    public void resetGameCreated() {
        this.gameCreated = false;
    }
}
