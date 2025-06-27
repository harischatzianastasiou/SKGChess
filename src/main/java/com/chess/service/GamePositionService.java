package com.chess.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.core.board.Board;
import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import com.chess.dto.rest.response.GamePositionDTO;
import com.chess.model.entity.Game;
import com.chess.model.entity.GamePosition;
import com.chess.repository.GamePositionRepository;
import com.chess.util.CompressionUtil;
import com.chess.util.ChessNotationUtil;

/**
 * Service class for managing game positions and move history
 * Handles storing and retrieving positions for move history viewing
 * Uses compression to reduce storage costs by 60-80%
 */
@Service
public class GamePositionService {
    
    @Autowired
    private GamePositionRepository gamePositionRepository;
    
    /**
     * Store a new position after a move is made
     * Compresses board state and move data to reduce storage costs
     * @param game The game entity
     * @param board The board state after the move
     * @param move The move that was made
     * @param moveNumber The move number (1, 2, 3, etc.)
     */
    public void storePosition(Game game, IBoard board, Move move, int moveNumber) {
        // Serialize the board state
        String boardState = board.serialize();
        
        // Serialize the move data
        String moveData = Board.serializeMove(move);
        
        // Generate algebraic notation for the move immediately
        String moveNotation = ChessNotationUtil.toAlgebraicNotation(move);
        
        // Compress the board state to reduce storage costs
        String compressedBoardState = CompressionUtil.safeCompress(boardState);
        
        // Compress the move data to reduce storage costs
        String compressedMoveData = CompressionUtil.safeCompress(moveData);
        
        // Log compression statistics for monitoring
        logCompressionStats("Board State", boardState, compressedBoardState);
        logCompressionStats("Move Data", moveData, compressedMoveData);
        
        // Create and save the new position with compressed data and move notation
        GamePosition position = new GamePosition(game, moveNumber, compressedBoardState, compressedMoveData, moveNotation);
        gamePositionRepository.save(position);
    }
    
    /**
     * Store the initial position when a game starts
     * Compresses board state to reduce storage costs
     * @param game The game entity
     * @param board The initial board state
     */
    public void storeInitialPosition(Game game, IBoard board) {
        // Serialize the board state
        String boardState = board.serialize();
        
        // Compress the board state to reduce storage costs
        String compressedBoardState = CompressionUtil.safeCompress(boardState);
        
        // Log compression statistics for monitoring
        logCompressionStats("Initial Board State", boardState, compressedBoardState);
        
        // Create and save the initial position with compressed data and null move notation
        GamePosition position = new GamePosition(game, 0, compressedBoardState, null, null);
        gamePositionRepository.save(position);
    }
    
    /**
     * Get all positions for a game (move history)
     * Automatically decompresses data when retrieving
     * @param gameId The ID of the game
     * @return List of positions ordered by move number
     */
    public List<GamePositionDTO> getGameHistory(String gameId) {
        // Retrieve all positions for the game
        List<GamePosition> positions = gamePositionRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        
        // Convert to DTOs (decompression happens in convertToDTO)
        return positions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get a specific position by move number
     * Automatically decompresses data when retrieving
     * @param gameId The ID of the game
     * @param moveNumber The move number to retrieve
     * @return Optional containing the position DTO if found
     */
    public Optional<GamePositionDTO> getPositionByMoveNumber(String gameId, int moveNumber) {
        // Find the position in the database
        Optional<GamePosition> position = gamePositionRepository.findByGameIdAndMoveNumber(gameId, moveNumber);
        
        // Convert to DTO if found (decompression happens in convertToDTO)
        return position.map(this::convertToDTO);
    }
    
    /**
     * Get the latest position for a game
     * Automatically decompresses data when retrieving
     * @param gameId The ID of the game
     * @return Optional containing the latest position DTO if found
     */
    public Optional<GamePositionDTO> getLatestPosition(String gameId) {
        // Find the latest position in the database
        Optional<GamePosition> position = gamePositionRepository.findLatestPositionByGameId(gameId);
        
        // Convert to DTO if found (decompression happens in convertToDTO)
        return position.map(this::convertToDTO);
    }
    
    /**
     * Get total number of positions for a game
     * @param gameId The ID of the game
     * @return Number of positions stored
     */
    public long getPositionCount(String gameId) {
        return gamePositionRepository.countByGameId(gameId);
    }
    
    /**
     * Convert GamePosition entity to GamePositionDTO
     * Automatically decompresses board state and move data
     * @param position The position entity
     * @return The position DTO
     */
    private GamePositionDTO convertToDTO(GamePosition position) {
        // Decompress the board state
        String decompressedBoardState = CompressionUtil.safeDecompress(position.getBoardState());
        // Decompress the move data (handle null for initial position)
        String decompressedMoveData = position.getMoveData() != null ? 
            CompressionUtil.safeDecompress(position.getMoveData()) : null;
        // Deserialize the board from decompressed data
        IBoard board = IBoard.deserialize(decompressedBoardState, decompressedMoveData);
        // Use the stored move notation directly
        String moveNotation = position.getMoveNotation();
        // Determine player alliance based on move number
        String playerAlliance = null;
        if (position.getMoveNumber() > 0) {
            // Even move numbers are white's moves, odd are black's
            playerAlliance = (position.getMoveNumber() % 2 == 1) ? "WHITE" : "BLACK";
        }
        return GamePositionDTO.builder()
            .id(position.getId())
            .moveNumber(position.getMoveNumber())
            .board(board)
            .moveData(decompressedMoveData) // Use decompressed move data
            .createdAt(position.getCreatedAt())
            .gameId(position.getGame().getId())
            .moveNotation(moveNotation) // Use stored move notation
            .playerAlliance(playerAlliance)
            .build();
    }
    
    /**
     * Delete all positions for a game (cleanup)
     * @param gameId The ID of the game
     */
    public void deleteGamePositions(String gameId) {
        gamePositionRepository.deleteByGameId(gameId);
    }
    
    /**
     * Log compression statistics for monitoring and debugging
     * @param dataType The type of data being compressed (e.g., "Board State", "Move Data")
     * @param originalData The original uncompressed data
     * @param compressedData The compressed data
     */
    private void logCompressionStats(String dataType, String originalData, String compressedData) {
        if (originalData != null && compressedData != null) {
            String stats = CompressionUtil.getCompressionStats(originalData, compressedData);
            System.out.println("[" + dataType + "] " + stats);
        }
    }
    
    /**
     * Get compression statistics for a specific position
     * Useful for monitoring compression effectiveness
     * @param gameId The ID of the game
     * @param moveNumber The move number
     * @return Compression statistics string
     */
    public String getPositionCompressionStats(String gameId, int moveNumber) {
        Optional<GamePosition> position = gamePositionRepository.findByGameIdAndMoveNumber(gameId, moveNumber);
        
        if (position.isPresent()) {
            GamePosition pos = position.get();
            String boardStats = CompressionUtil.getCompressionStats(
                CompressionUtil.safeDecompress(pos.getBoardState()), 
                pos.getBoardState()
            );
            String moveStats = CompressionUtil.getCompressionStats(
                CompressionUtil.safeDecompress(pos.getMoveData()), 
                pos.getMoveData()
            );
            
            return "Board: " + boardStats + ", Move: " + moveStats;
        }
        
        return "Position not found";
    }
} 