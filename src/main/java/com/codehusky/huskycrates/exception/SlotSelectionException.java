package com.codehusky.huskycrates.exception;

public class SlotSelectionException extends RuntimeException {
    private final String message;

    public SlotSelectionException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
