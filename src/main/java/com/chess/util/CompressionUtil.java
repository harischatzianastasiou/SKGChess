package com.chess.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for compressing and decompressing board data
 * Reduces storage costs by compressing JSON board representations
 * Uses GZIP compression which typically reduces JSON size by 60-80%
 */
public class CompressionUtil {
    
    /**
     * Compresses a string (typically JSON) using GZIP compression
     * @param data The string data to compress
     * @return Base64 encoded compressed string
     */
    public static String compress(String data) {
        if (data == null || data.isEmpty()) {
            return data; // Return as-is if null or empty
        }
        
        try {
            // Create byte array output stream to hold compressed data
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            
            // Create GZIP output stream for compression
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                // Write the original string data to the GZIP stream
                gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
            }
            
            // Get the compressed byte array
            byte[] compressedBytes = byteArrayOutputStream.toByteArray();
            
            // Encode the compressed bytes to Base64 string for database storage
            return Base64.getEncoder().encodeToString(compressedBytes);
            
        } catch (IOException e) {
            // If compression fails, return original data
            System.err.println("Compression failed, returning original data: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * Decompresses a Base64 encoded compressed string back to original data
     * @param compressedData The compressed Base64 string
     * @return The original uncompressed string
     */
    public static String decompress(String compressedData) {
        if (compressedData == null || compressedData.isEmpty()) {
            return compressedData; // Return as-is if null or empty
        }
        
        try {
            // Decode Base64 string to compressed bytes
            byte[] compressedBytes = Base64.getDecoder().decode(compressedData);
            
            // Create byte array input stream from compressed bytes
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedBytes);
            
            // Create GZIP input stream for decompression
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
                // Read all decompressed bytes
                byte[] decompressedBytes = gzipInputStream.readAllBytes();
                
                // Convert bytes back to string
                return new String(decompressedBytes, StandardCharsets.UTF_8);
            }
            
        } catch (IOException e) {
            // If decompression fails, return original data (might be uncompressed)
            System.err.println("Decompression failed, returning original data: " + e.getMessage());
            return compressedData;
        }
    }
    
    /**
     * Checks if a string is compressed (Base64 encoded GZIP)
     * @param data The string to check
     * @return true if the string appears to be compressed
     */
    public static boolean isCompressed(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        try {
            // Try to decode as Base64
            byte[] bytes = Base64.getDecoder().decode(data);
            
            // Check if it starts with GZIP magic number (0x1f 0x8b)
            return bytes.length >= 2 && bytes[0] == 0x1f && bytes[1] == (byte) 0x8b;
            
        } catch (IllegalArgumentException e) {
            // Not valid Base64, so not compressed
            return false;
        }
    }
    
    /**
     * Gets compression ratio information for debugging
     * @param originalData The original uncompressed data
     * @param compressedData The compressed data
     * @return String with compression statistics
     */
    public static String getCompressionStats(String originalData, String compressedData) {
        if (originalData == null || compressedData == null) {
            return "Cannot calculate stats: null data";
        }
        
        int originalSize = originalData.getBytes(StandardCharsets.UTF_8).length;
        int compressedSize = compressedData.getBytes(StandardCharsets.UTF_8).length;
        double compressionRatio = (double) compressedSize / originalSize * 100;
        int bytesSaved = originalSize - compressedSize;
        
        return String.format("Original: %d bytes, Compressed: %d bytes, Ratio: %.1f%%, Saved: %d bytes", 
                           originalSize, compressedSize, compressionRatio, bytesSaved);
    }
    
    /**
     * Safely compresses data with fallback to original if compression fails
     * @param data The data to compress
     * @return Compressed data or original data if compression fails
     */
    public static String safeCompress(String data) {
        try {
            String compressed = compress(data);
            
            // Only use compressed version if it's actually smaller
            if (compressed != null && compressed.length() < data.length()) {
                return compressed;
            } else {
                return data; // Use original if compression doesn't help
            }
            
        } catch (Exception e) {
            System.err.println("Safe compression failed, using original data: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * Safely decompresses data, handling both compressed and uncompressed data
     * @param data The data to decompress
     * @return Decompressed data or original data if not compressed
     */
    public static String safeDecompress(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        // Check if data is compressed
        if (isCompressed(data)) {
            try {
                return decompress(data);
            } catch (Exception e) {
                System.err.println("Safe decompression failed, using original data: " + e.getMessage());
                return data;
            }
        } else {
            // Data is not compressed, return as-is
            return data;
        }
    }
} 