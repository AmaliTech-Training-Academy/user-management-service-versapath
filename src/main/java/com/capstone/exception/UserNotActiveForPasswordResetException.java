package com.capstone.exception;

public class UserNotActiveForPasswordResetException extends RuntimeException {
    public UserNotActiveForPasswordResetException(String message) {
        super(message);
    }
}