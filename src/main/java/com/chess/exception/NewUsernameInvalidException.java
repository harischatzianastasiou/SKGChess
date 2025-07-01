package com.chess.exception;

public class NewUsernameInvalidException extends RuntimeException {
    public NewUsernameInvalidException(String message) {
        super(message);
    }
}
