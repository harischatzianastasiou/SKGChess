package com.chess.exception;

public class UsernameChangesLeftException extends RuntimeException {
    public UsernameChangesLeftException(String message) {
        super(message);
    }
}
