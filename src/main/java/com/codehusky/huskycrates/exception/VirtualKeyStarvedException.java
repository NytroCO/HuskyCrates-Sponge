package com.codehusky.huskycrates.exception;

public class VirtualKeyStarvedException extends RuntimeException {
    private final String message;

    public VirtualKeyStarvedException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
