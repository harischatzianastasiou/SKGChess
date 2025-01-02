package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;


public class ProtectedCoordinatesTracker {
    private static final List<Integer> protectedCoordinates = new ArrayList<>();

    public static void addProtectedCoordinate(int coordinate) {
        protectedCoordinates.add(coordinate);
    }

    public static List<Integer> getProtectedCoordinates(){
        return ImmutableList.copyOf(protectedCoordinates);
    }

    public static void clear() {
        protectedCoordinates.clear(); // Clear the set for the next calculation
    }
} 