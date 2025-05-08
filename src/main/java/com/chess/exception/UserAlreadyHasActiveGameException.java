package com.chess.exception;

public class UserAlreadyHasActiveGameException extends RuntimeException {
    public UserAlreadyHasActiveGameException(String message) {
        super(message);
    }
}
