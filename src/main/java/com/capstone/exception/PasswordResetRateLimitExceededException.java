package com.capstone.exception;

public class PasswordResetRateLimitExceededException extends RuntimeException {
    public PasswordResetRateLimitExceededException(String message) {
        super(message);
    }
}