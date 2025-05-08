package com.example.micro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there's an error updating a workload
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WorkloadUpdateException extends RuntimeException {

    public WorkloadUpdateException(String message) {
        super(message);
    }

    public WorkloadUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}