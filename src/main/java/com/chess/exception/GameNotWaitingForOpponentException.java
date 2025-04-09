package com.chess.exception;

public class GameNotWaitingForOpponentException extends RuntimeException {
    public GameNotWaitingForOpponentException(String gameId) {
        super("Game with ID " + gameId + " is not waiting for an opponent.");
    }
}
