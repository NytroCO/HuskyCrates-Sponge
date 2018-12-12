package com.codehusky.huskycrates.exception;

public class ConfigParseException extends ConfigException {
    public ConfigParseException(final String message, final Object[] path) {
        super(message, path);
    }
}