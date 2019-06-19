package com.monjam.core.api;

public class MonJamException extends RuntimeException {
    public MonJamException() {
    }

    public MonJamException(String message) {
        super(message);
    }

    public MonJamException(String message, Throwable cause) {
        super(message, cause);
    }
}
