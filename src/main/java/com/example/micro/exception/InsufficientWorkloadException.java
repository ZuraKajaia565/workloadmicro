package com.example.micro.exception;

public class InsufficientWorkloadException extends RuntimeException {

    public InsufficientWorkloadException(String message) {
        super(message);
    }

    public InsufficientWorkloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
