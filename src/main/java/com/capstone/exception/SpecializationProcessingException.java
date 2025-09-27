package com.capstone.exception;

public class SpecializationProcessingException extends RuntimeException {
    public SpecializationProcessingException(String message) {
        super(message);
    }

    public SpecializationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
