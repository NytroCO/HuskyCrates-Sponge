package com.codehusky.huskycrates.exception;

public class InjectionDataException extends Exception {
    private final String message;

    public InjectionDataException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
