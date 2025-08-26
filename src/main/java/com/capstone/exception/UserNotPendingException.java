package com.capstone.exception;

public class UserNotPendingException extends RuntimeException{
    public UserNotPendingException(String message) {
        super(message);
    }
    public UserNotPendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
