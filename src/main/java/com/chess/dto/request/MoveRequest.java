package com.chess.dto.request;

public class MoveRequest {

    private final int sourceCoordinate;
    private final int targetCoordinate;

    public MoveRequest(int sourceCoordinate, int targetCoordinate) {
        this.sourceCoordinate = sourceCoordinate;
        this.targetCoordinate = targetCoordinate;
    }

    public int getSourceCoordinate() {
        return sourceCoordinate;
    }

    public int getTargetCoordinate() {
        return targetCoordinate;
    }
}
