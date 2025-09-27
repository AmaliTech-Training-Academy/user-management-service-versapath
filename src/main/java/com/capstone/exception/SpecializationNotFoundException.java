package com.capstone.exception;

public class SpecializationNotFoundException extends RuntimeException {
    public SpecializationNotFoundException(String message) {
        super(message);
    }

    public SpecializationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
