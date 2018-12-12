package com.codehusky.huskycrates.exception;

public class InjectionMissedException extends RuntimeException {
    private final String message;

    public InjectionMissedException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
