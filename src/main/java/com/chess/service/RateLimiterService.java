package com.chess.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class RateLimiterService {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);
    
    // Store request counts per IP address
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    // Rate limit configuration
    private static final int MAX_REQUESTS_PER_HOUR = 1000; // Maximum requests per hour
    private static final Duration WINDOW_SIZE = Duration.ofHours(1); // Time window for rate limiting
    
    /**
     * Check if a request from an IP address should be rate limited
     * @param ipAddress The IP address of the requester
     * @return true if the request should be allowed, false if it should be rate limited
     */
    public boolean isAllowed(String ipAddress) {
        RequestCounter counter = requestCounters.computeIfAbsent(ipAddress, k -> new RequestCounter());
        return counter.incrementAndCheck();
    }
    
    /**
     * Inner class to track request counts within a time window
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private LocalDateTime windowStart = LocalDateTime.now();
        
        public synchronized boolean incrementAndCheck() {
            LocalDateTime now = LocalDateTime.now();
            
            // If we're outside the window, reset the counter
            if (Duration.between(windowStart, now).compareTo(WINDOW_SIZE) > 0) {
                count.set(0);
                windowStart = now;
            }
            
            // Increment and check if we're under the limit
            int currentCount = count.incrementAndGet();
            return currentCount <= MAX_REQUESTS_PER_HOUR;
        }
    }
} 