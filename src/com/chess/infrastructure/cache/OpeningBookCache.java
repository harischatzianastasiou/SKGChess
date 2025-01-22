package com.chess.infrastructure.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OpeningBookCache {
    private static final String CACHE_FILE = "opening_book.cache.gz";
    private static final Gson gson = new Gson();
    private static final Type BOOK_TYPE = new TypeToken<Map<String, List<String>>>(){}.getType();
    private static final int BUFFER_SIZE = 8192 * 4; // 32KB buffer
    
    public static void saveToFile(Map<String, List<String>> openingBook) {
        System.out.println("Saving opening book to cache file...");
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new GZIPOutputStream(
                        new BufferedOutputStream(
                            new FileOutputStream(CACHE_FILE), BUFFER_SIZE)), 
                    StandardCharsets.UTF_8))) {
            
            gson.toJson(openingBook, writer);
            System.out.println("Opening book cached successfully!");
        } catch (IOException e) {
            System.err.println("Error saving opening book cache: " + e.getMessage());
        }
    }
    
    public static Map<String, List<String>> loadFromFile() {
        if (!new File(CACHE_FILE).exists()) {
            return null;
        }
        
        System.out.println("Loading opening book from cache file...");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new GZIPInputStream(
                        new BufferedInputStream(
                            new FileInputStream(CACHE_FILE), BUFFER_SIZE)), 
                    StandardCharsets.UTF_8))) {
            
            Map<String, List<String>> book = gson.fromJson(reader, BOOK_TYPE);
            System.out.printf("Opening book loaded from cache (%d positions)%n", book.size());
            return book;
        } catch (IOException e) {
            System.err.println("Error loading opening book cache: " + e.getMessage());
            // If there's an error loading the cache, delete it
            new File(CACHE_FILE).delete();
            return null;
        }
    }
    
    public static boolean isCacheValid() {
        File cacheFile = new File(CACHE_FILE);
        if (!cacheFile.exists()) {
            return false;
        }
        
        File dbFile = new File("chess_games.db");
        if (!dbFile.exists()) {
            return false;
        }
        
        // Cache is valid if it's newer than the database file
        return cacheFile.lastModified() > dbFile.lastModified();
    }
    
    public static void invalidateCache() {
        File cacheFile = new File(CACHE_FILE);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }
} 