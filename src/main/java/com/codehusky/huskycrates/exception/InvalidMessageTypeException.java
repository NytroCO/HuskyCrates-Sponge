package com.codehusky.huskycrates.exception;

public class InvalidMessageTypeException extends RuntimeException {
    private final String message;

    public InvalidMessageTypeException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
