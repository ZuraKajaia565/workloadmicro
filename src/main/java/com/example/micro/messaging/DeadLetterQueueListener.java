package com.example.micro.messaging;

import com.example.micro.config.JmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Listener for messages that were sent to the Dead Letter Queue
 * due to processing errors.
 */
@Component
public class DeadLetterQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueListener.class);

    @JmsListener(destination = JmsConfig.WORKLOAD_DLQ)
    public void processDeadLetterMessage(
            @Payload WorkloadMessage message,
            @Header(name = "error.reason", defaultValue = "Unknown") String errorReason,
            @Header(name = "original.transaction.id", defaultValue = "N/A") String originalTransactionId,
            @Header(name = "error.timestamp", defaultValue = "0") String errorTimestamp) {

        logger.warn("Processing Dead Letter Queue message: {}", message);
        logger.warn("Error reason: {}", errorReason);
        logger.warn("Original transaction ID: {}", originalTransactionId);

        // Convert timestamp to readable format if available
        if (!"0".equals(errorTimestamp)) {
            try {
                long timestamp = Long.parseLong(errorTimestamp);
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault());
                logger.warn("Error timestamp: {}", dateTime);
            } catch (NumberFormatException e) {
                logger.warn("Invalid error timestamp format: {}", errorTimestamp);
            }
        }

        // Additional processing could include:
        // 1. Storing the error in a database for analysis
        // 2. Sending notifications to administrators
        // 3. Implementing a retry mechanism for specific errors
        // 4. Forwarding to a specialized error handler service
    }
}