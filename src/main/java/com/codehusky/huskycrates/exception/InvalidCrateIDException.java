package com.codehusky.huskycrates.exception;

public class InvalidCrateIDException extends RuntimeException {
    private final String message;

    public InvalidCrateIDException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
