package com.codehusky.huskycrates.exception;

public class NoMessageContextException extends RuntimeException {
    private final String message;

    public NoMessageContextException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
