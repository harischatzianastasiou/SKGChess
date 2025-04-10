package com.chess.exception;

public class GameAlreadyJoinedException extends RuntimeException {
    public GameAlreadyJoinedException(String gameId) {
        super("Game with ID " + gameId + " is already joined");
    }
}
