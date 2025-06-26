package com.chess.service;

import org.springframework.stereotype.Service;

import com.chess.core.board.IBoard;
import com.chess.util.CompressionUtil;

/**
 * Service for handling compression of Game entity board data
 * Provides methods to compress and decompress board data for storage optimization
 */
@Service
public class GameCompressionService {
    
    /**
     * Compress board data for storage in the Game entity
     * Reduces storage costs by 60-80% using GZIP compression
     * 
     * @param board The board to compress
     * @return Compressed board string ready for database storage
     */
    public String compressBoardForStorage(IBoard board) {
        if (board == null) {
            return null;
        }
        
        try {
            // Serialize the board to JSON string
            String serializedBoard = board.serialize();
            
            // Compress the serialized board
            String compressedBoard = CompressionUtil.safeCompress(serializedBoard);
            
            // Log compression statistics for monitoring
            System.out.println("Game Board Compression: " + 
                CompressionUtil.getCompressionStats(serializedBoard, compressedBoard));
            
            return compressedBoard;
            
        } catch (Exception e) {
            System.err.println("Failed to compress board for storage: " + e.getMessage());
            // Fallback to compressed serialization even if safeCompress fails
            try {
                String serializedBoard = board.serialize();
                return CompressionUtil.compress(serializedBoard);
            } catch (Exception fallbackException) {
                System.err.println("Fallback compression also failed: " + fallbackException.getMessage());
                // Last resort: return uncompressed data
                return board.serialize();
            }
        }
    }
    
    /**
     * Decompress board data from storage in the Game entity
     * Automatically handles both compressed and uncompressed data
     * 
     * @param storedBoardData The board data from database (compressed or uncompressed)
     * @return Decompressed board string ready for deserialization
     */
    public String decompressBoardFromStorage(String storedBoardData) {
        if (storedBoardData == null || storedBoardData.isEmpty()) {
            return storedBoardData;
        }
        
        try {
            // Safely decompress the stored board data
            String decompressedBoard = CompressionUtil.safeDecompress(storedBoardData);
            
            return decompressedBoard;
            
        } catch (Exception e) {
            System.err.println("Failed to decompress board from storage: " + e.getMessage());
            // Return original data if decompression fails
            return storedBoardData;
        }
    }
    
    /**
     * Get compression statistics for a stored board
     * Useful for monitoring compression effectiveness
     * 
     * @param storedBoardData The board data from database
     * @return Compression statistics string
     */
    public String getBoardCompressionStats(String storedBoardData) {
        if (storedBoardData == null || storedBoardData.isEmpty()) {
            return "No board data to analyze";
        }
        
        try {
            String decompressedBoard = CompressionUtil.safeDecompress(storedBoardData);
            return CompressionUtil.getCompressionStats(decompressedBoard, storedBoardData);
            
        } catch (Exception e) {
            return "Failed to analyze compression: " + e.getMessage();
        }
    }
    
    /**
     * Check if stored board data is compressed
     * 
     * @param storedBoardData The board data from database
     * @return true if the data is compressed
     */
    public boolean isBoardCompressed(String storedBoardData) {
        return CompressionUtil.isCompressed(storedBoardData);
    }
    
    /**
     * Compress board data with size validation
     * Only uses compression if it actually reduces size
     * 
     * @param board The board to compress
     * @return Compressed board string or original if compression doesn't help
     */
    public String compressBoardIfBeneficial(IBoard board) {
        if (board == null) {
            return null;
        }
        
        try {
            // Serialize the board
            String serializedBoard = board.serialize();
            
            // Compress the board
            String compressedBoard = CompressionUtil.compress(serializedBoard);
            
            // Only use compression if it actually reduces size
            if (compressedBoard != null && compressedBoard.length() < serializedBoard.length()) {
                System.out.println("Using compressed board: " + 
                    CompressionUtil.getCompressionStats(serializedBoard, compressedBoard));
                return compressedBoard;
            } else {
                System.out.println("Using uncompressed board (compression not beneficial)");
                return serializedBoard;
            }
            
        } catch (Exception e) {
            System.err.println("Compression failed, trying safe compression: " + e.getMessage());
            // Try safe compression as fallback
            try {
                String serializedBoard = board.serialize();
                String safeCompressed = CompressionUtil.safeCompress(serializedBoard);
                if (safeCompressed != null && safeCompressed.length() < serializedBoard.length()) {
                    return safeCompressed;
                }
            } catch (Exception safeException) {
                System.err.println("Safe compression also failed: " + safeException.getMessage());
            }
            // Last resort: return uncompressed data
            return board.serialize();
        }
    }
} 