package com.example.micro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to subtract more workload than available.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientWorkloadException extends RuntimeException {

    public InsufficientWorkloadException(String message) {
        super(message);
    }

    public InsufficientWorkloadException(String message, Throwable cause) {
        super(message, cause);
    }
}