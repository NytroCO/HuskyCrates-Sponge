package com.codehusky.huskycrates.exception;

public class RewardDeliveryException extends RuntimeException {
    private final String message;

    public RewardDeliveryException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
