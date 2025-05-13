package com.example.micro.messaging;

import com.example.micro.config.JmsConfig;
import com.example.micro.exception.MessageProcessingException;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.service.WorkloadService;
import com.example.micro.service.WorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkloadMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadMessageListener.class);

    private final WorkloadService workloadService;
    private final WorkloadService workloadMongoService;
    private final JmsTemplate jmsTemplate;
    private final MessageValidator messageValidator;

    @Autowired
    public WorkloadMessageListener(
            WorkloadService workloadService,
            WorkloadService workloadMongoService,
            JmsTemplate jmsTemplate,
            MessageValidator messageValidator) {
        this.workloadService = workloadService;
        this.workloadMongoService = workloadMongoService;
        this.jmsTemplate = jmsTemplate;
        this.messageValidator = messageValidator;
    }

    /**
     * Processes incoming workload messages from the workload queue.
     * Uses concurrent consumers for horizontal scaling.
     *
     * @param message The workload message to process
     * @param headers Message headers
     * @param jmsMessage The raw JMS message
     */
    @JmsListener(
            destination = JmsConfig.WORKLOAD_QUEUE,
            containerFactory = "jmsListenerContainerFactory"
    )
    public void processWorkloadMessage(
            @Payload WorkloadMessage message,
            @Headers MessageHeaders headers,
            Message jmsMessage) {

        String transactionId = message.getTransactionId();
        MDC.put("transactionId", transactionId);

        logger.info("Received workload message with transaction ID: {}", transactionId);
        logger.debug("Message details: {}", message);
        logger.debug("Message headers: {}", headers);

        try {
            // Validate message
            if (!validateMessage(message)) {
                return;
            }

            // Process message for relational database
            processMessageForRelationalDB(message);

            // Process message for MongoDB
            workloadMongoService.processWorkloadMessage(message);

            // Acknowledge message on successful processing
            acknowledgeMessage(jmsMessage, transactionId);
        } catch (Exception e) {
            handleProcessingException(e, message);
        } finally {
            MDC.clear();
        }
    }

    private boolean validateMessage(WorkloadMessage message) {
        List<String> validationErrors = messageValidator.validateWorkloadMessage(message);
        if (!validationErrors.isEmpty()) {
            String errorReason = String.join("; ", validationErrors);
            logger.warn("Message validation failed: {}", errorReason);
            handleInvalidMessage(message, errorReason);
            return false;
        }
        return true;
    }

    private void processMessageForRelationalDB(WorkloadMessage message) {
        logger.info("Processing message for relational database: trainer={}, period={}/{}",
                message.getUsername(), message.getYear(), message.getMonth());

        try {
            // Process message based on type
            switch (message.getMessageType()) {
                case CREATE_UPDATE:
                    processCreateUpdateMessage(message);
                    break;
                case DELETE:
                    processDeleteMessage(message);
                    break;
                default:
                    handleInvalidMessage(message, "Unknown message type: " + message.getMessageType());
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing message for relational database: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Processes a message to create or update a workload record
     *
     * @param message The message containing workload information
     * @throws MessageProcessingException If processing fails
     */
    private void processCreateUpdateMessage(WorkloadMessage message) throws MessageProcessingException {
        logger.info("Processing CREATE/UPDATE workload for trainer: {}, period: {}/{}",
                message.getUsername(), message.getYear(), message.getMonth());

        try {
            // Update or create workload
            workloadService.updateOrCreateWorkload(
                    message.getUsername(),
                    message.getYear(),
                    message.getMonth(),
                    message.getFirstName(),
                    message.getLastName(),
                    message.isActive(),
                    message.getTrainingDuration()
            );

            logger.info("Workload created/updated successfully for trainer: {}", message.getUsername());
        } catch (Exception e) {
            logger.error("Failed to create/update workload: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to create/update workload: " + e.getMessage(), e);
        }
    }

    /**
     * Processes a message to delete a workload record
     *
     * @param message The message containing workload information
     * @throws ResourceNotFoundException If the workload does not exist
     */
    private void processDeleteMessage(WorkloadMessage message) {
        logger.info("Processing DELETE workload for trainer: {}, period: {}/{}",
                message.getUsername(), message.getYear(), message.getMonth());

        try {
            workloadService.deleteWorkload(
                    message.getUsername(),
                    message.getYear(),
                    message.getMonth()
            );
            logger.info("Workload deleted successfully for trainer: {}", message.getUsername());
        } catch (ResourceNotFoundException e) {
            logger.warn("Workload not found for deletion: {}", e.getMessage());
            // This is not considered an error - the resource is already gone
        } catch (Exception e) {
            logger.error("Unexpected error during workload deletion: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to delete workload: " + e.getMessage(), e);
        }
    }

    private void acknowledgeMessage(Message jmsMessage, String transactionId) {
        try {
            jmsMessage.acknowledge();
            logger.info("Message processed successfully, transaction ID: {}", transactionId);
        } catch (JMSException e) {
            logger.error("Error acknowledging message: {}", e.getMessage(), e);
        }
    }

    private void handleProcessingException(Exception e, WorkloadMessage message) {
        if (e instanceof ResourceNotFoundException) {
            // Handle resource not found - this is a "business" exception, not a system error
            logger.warn("Resource not found while processing message: {}", e.getMessage());
            // No need to send to DLQ - it's an expected scenario
        } else if (e instanceof MessageProcessingException) {
            // Handle specific message processing exceptions
            logger.error("Message processing failed: {}", e.getMessage(), e);
            handleInvalidMessage(message, "Processing error: " + e.getMessage());
        } else {
            // Handle unexpected exceptions
            logger.error("Unexpected error while processing message: {}", e.getMessage(), e);
            handleInvalidMessage(message, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Handles invalid messages by sending them to the Dead Letter Queue
     * with appropriate error information.
     *
     * @param message The invalid message
     * @param reason The reason why the message is invalid
     */
    private void handleInvalidMessage(WorkloadMessage message, String reason) {
        logger.error("Invalid message received: {}. Reason: {}", message, reason);

        // Send to Dead Letter Queue
        try {
            jmsTemplate.convertAndSend(JmsConfig.WORKLOAD_DLQ, message, m -> {
                m.setStringProperty("error.reason", reason);
                m.setStringProperty("original.transaction.id", message.getTransactionId());
                m.setStringProperty("error.timestamp", String.valueOf(System.currentTimeMillis()));
                return m;
            });
            logger.info("Message sent to Dead Letter Queue (DLQ): {}", message.getTransactionId());
        } catch (Exception e) {
            logger.error("Failed to send message to DLQ: {}", e.getMessage(), e);
        }
    }
}