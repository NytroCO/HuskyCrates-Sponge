package com.codehusky.huskycrates.exception;

public class ItemBuildException extends Exception {
    private final String message;

    public ItemBuildException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
