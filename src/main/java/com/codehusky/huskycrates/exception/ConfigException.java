package com.codehusky.huskycrates.exception;

public class ConfigException extends RuntimeException {

    private String message;

    ConfigException(final String message, final Object[] path) {
        this.message = message;
        this.message += " Issue can be located at " + readablePath(path);
    }

    public static String readablePath(Object[] path) {
        StringBuilder readablePath = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            readablePath.append(path[i]);
            if (i + 1 < path.length) {
                readablePath.append(".");
            }
        }
        return readablePath.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
