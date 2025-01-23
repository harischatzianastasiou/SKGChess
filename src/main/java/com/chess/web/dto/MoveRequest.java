package com.chess.web.dto;

public class MoveRequest {
    private int sourceCoordinate;
    private int targetCoordinate;

    // Default constructor for JSON deserialization
    public MoveRequest() {}

    public MoveRequest(int sourceCoordinate, int targetCoordinate) {
        this.sourceCoordinate = sourceCoordinate;
        this.targetCoordinate = targetCoordinate;
    }

    public int getSourceCoordinate() {
        return sourceCoordinate;
    }

    public void setSourceCoordinate(int sourceCoordinate) {
        this.sourceCoordinate = sourceCoordinate;
    }

    public int getTargetCoordinate() {
        return targetCoordinate;
    }

    public void setTargetCoordinate(int targetCoordinate) {
        this.targetCoordinate = targetCoordinate;
    }
} 