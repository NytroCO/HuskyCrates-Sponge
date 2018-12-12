package com.codehusky.huskycrates.exception;

public class DoubleRegistrationException extends RuntimeException {
    private final String message;

    public DoubleRegistrationException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
