package com.chess.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.dto.rest.response.GamePositionDTO;
import com.chess.service.GamePositionService;

/**
 * Controller for handling move history viewing functionality
 * Allows players to shuffle through each move played in a game
 */
@RestController
@RequestMapping("/api/games")
public class GameHistoryController {
    
    @Autowired
    private GamePositionService gamePositionService;
    
    /**
     * Get the complete move history for a game
     * @param gameId The ID of the game
     * @return List of all positions in chronological order
     */
    @GetMapping("/{gameId}/history")
    public ResponseEntity<List<GamePositionDTO>> getGameHistory(@PathVariable String gameId) {
        try {
            // Retrieve the complete move history for the game
            List<GamePositionDTO> history = gamePositionService.getGameHistory(gameId);
            
            // Return the history as JSON response
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            // Return error response if something goes wrong
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get a specific position by move number
     * @param gameId The ID of the game
     * @param moveNumber The move number to retrieve
     * @return The position at the specified move number
     */
    @GetMapping("/{gameId}/history/{moveNumber}")
    public ResponseEntity<GamePositionDTO> getPositionByMoveNumber(
            @PathVariable String gameId, 
            @PathVariable int moveNumber) {
        try {
            // Retrieve the specific position
            Optional<GamePositionDTO> position = gamePositionService.getPositionByMoveNumber(gameId, moveNumber);
            
            // Return the position if found, otherwise return 404
            return position.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            // Return error response if something goes wrong
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get the latest position for a game
     * @param gameId The ID of the game
     * @return The most recent position
     */
    @GetMapping("/{gameId}/history/latest")
    public ResponseEntity<GamePositionDTO> getLatestPosition(@PathVariable String gameId) {
        try {
            // Retrieve the latest position
            Optional<GamePositionDTO> position = gamePositionService.getLatestPosition(gameId);
            
            // Return the position if found, otherwise return 404
            return position.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            // Return error response if something goes wrong
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get the total number of positions stored for a game
     * @param gameId The ID of the game
     * @return The count of positions
     */
    @GetMapping("/{gameId}/history/count")
    public ResponseEntity<Long> getPositionCount(@PathVariable String gameId) {
        try {
            // Get the total number of positions
            long count = gamePositionService.getPositionCount(gameId);
            
            // Return the count
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            // Return error response if something goes wrong
            return ResponseEntity.badRequest().build();
        }
    }
} 