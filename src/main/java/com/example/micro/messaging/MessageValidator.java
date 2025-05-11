package com.example.micro.messaging;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates message data to ensure it meets requirements
 */
@Component
public class MessageValidator {

    /**
     * Validates a WorkloadMessage
     *
     * @param message The message to validate
     * @return A list of validation errors, empty if valid
     */
    public List<String> validateWorkloadMessage(WorkloadMessage message) {
        List<String> errors = new ArrayList<>();

        // Basic validation
        if (message == null) {
            errors.add("Message is null");
            return errors;
        }

        // Required fields validation
        if (message.getUsername() == null || message.getUsername().isEmpty()) {
            errors.add("Username is required");
        }

        if (message.getFirstName() == null || message.getFirstName().isEmpty()) {
            errors.add("First name is required");
        }

        if (message.getLastName() == null || message.getLastName().isEmpty()) {
            errors.add("Last name is required");
        }

        if (message.getYear() <= 0) {
            errors.add("Year must be a positive value");
        }

        if (message.getMonth() <= 0 || message.getMonth() > 12) {
            errors.add("Month must be between 1 and 12");
        }

        if (message.getMessageType() == null) {
            errors.add("Message type is required");
        }

        // Business rules validation
        if (message.getMessageType() == WorkloadMessage.MessageType.CREATE_UPDATE
                && message.getTrainingDuration() <= 0) {
            errors.add("Training duration must be positive for CREATE_UPDATE operations");
        }

        return errors;
    }
}