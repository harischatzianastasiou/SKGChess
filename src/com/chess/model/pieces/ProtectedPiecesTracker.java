package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;


public class ProtectedPiecesTracker {
    private static final List<Integer> protectedPieceCoordinates = new ArrayList<>();

    public static void addProtectedPieceCoordinate(int pieceCoordinate) {
        protectedPieceCoordinates.add(pieceCoordinate);
    }

    public static List<Integer> getProtectedPieceCoordinate(){
        return ImmutableList.copyOf(protectedPieceCoordinates);
    }

    public static void clear() {
        protectedPieceCoordinates.clear(); // Clear the set for the next calculation
    }
} 