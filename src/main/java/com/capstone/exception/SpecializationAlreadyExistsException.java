package com.capstone.exception;

public class SpecializationAlreadyExistsException extends RuntimeException {
    public SpecializationAlreadyExistsException(String message) {
        super(message);
    }

    public SpecializationAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
