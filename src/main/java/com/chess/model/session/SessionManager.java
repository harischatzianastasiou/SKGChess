package com.chess.model.session;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.chess.model.entity.User;
import com.chess.service.UserService;

import lombok.Data;

@Component
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final UserService userService;
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();
    private final PriorityQueue<QueuedUser> matchmakingQueue = new PriorityQueue<>();
    private final Map<String, String> sessionToUserId = new ConcurrentHashMap<>();
    private boolean gameCreated = false; // flag to check if a game has been created. needs careful handling.
    
    public SessionManager(UserService userService) {
        this.userService = userService;
    }

    @Data
    public class QueuedUser implements Comparable<QueuedUser> {
        private final String username;
        private final String sessionId;
        private final LocalDateTime joinedAt;

        @Override
        public int compareTo(QueuedUser other) {
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
        sessionToUserId.remove(sessionId);
        removeFromMatchmakingQueue(sessionId);
    }

    public void addToMatchmakingQueue(String sessionId, String username) {
        logger.info("Adding to matchmaking queue - sessionId: {}, username: {}", sessionId, username);
        if (isSessionActive(sessionId)) {
            // Find player by username and store their ID
            User user = userService.getUserByUsername(username);
            sessionToUserId.put(sessionId, user.getId().toString());
            
            matchmakingQueue.offer(new QueuedUser(username, sessionId, LocalDateTime.now()));
            tryMatchUsers();
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

    private synchronized boolean tryMatchUsers() {
        logger.info("Trying to match players - current queue size: {}", getQueueSize());
        if (getQueueSize() >= 2) {
            QueuedUser user1 = pollQueue();
            QueuedUser user2 = pollQueue();
            
            if (user1 != null && user2 != null) {
                logger.info("Found match - user1: {}, user2: {}", user1.getUsername(), user2.getUsername());
                
                // Remove players from queue first to prevent duplicate matches
                removeFromMatchmakingQueue(user1.getSessionId());
                removeFromMatchmakingQueue(user2.getSessionId());
                
                // Randomly assign colors
                if (Math.random() < 0.5) {
                    QueuedUser temp = user1;
                    user1 = user2;
                    user2 = temp;
                }
                
                String user1Id = sessionToUserId.get(user1.getSessionId());
                String user2Id = sessionToUserId.get(user2.getSessionId());
                
                if(user1Id != null && user2Id != null) {
                    this.gameCreated = true;
                }
            }
        }
        return this.gameCreated;
    }

    public boolean isUserInQueue(String sessionId) {
        return matchmakingQueue.stream()
                .anyMatch(qp -> qp.getSessionId().equals(sessionId));
    }

    public void addUserToSession(String sessionId, String playerId) {
        logger.info("Adding player to session - playerId: {}, sessionId: {}", playerId, sessionId);
        sessionToUserId.put(sessionId, playerId);
        activeSessions.add(sessionId);
    }

    public void removeUserFromSession(String sessionId) {
        sessionToUserId.remove(sessionId);
    }

    public QueuedUser pollQueue() {
        return matchmakingQueue.poll();
    }

    public int getQueueSize() {
        return matchmakingQueue.size();
    }

    public String getUserIdFromSession(String sessionId) {
        return sessionToUserId.get(sessionId);
    }

    public boolean isGameCreated() {
        return gameCreated;
    }

    public void resetGameCreated() {
        this.gameCreated = false;
    }
}
