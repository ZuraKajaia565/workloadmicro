package com.example.micro.service;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.messaging.WorkloadMessage;
import com.example.micro.repository.TrainerWorkloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadService.class);

    private final TrainerWorkloadRepository workloadRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public WorkloadService(TrainerWorkloadRepository workloadRepository, MongoTemplate mongoTemplate) {
        this.workloadRepository = workloadRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Process workload message
     * @param message The workload message with trainer and training info
     */
    public void processWorkloadMessage(WorkloadMessage message) {
        String transactionId = message.getTransactionId();
        MDC.put("transactionId", transactionId);

        logger.info("Processing MongoDB workload for trainer: {}, period: {}/{}, transaction ID: {}",
                message.getUsername(), message.getYear(), message.getMonth(), transactionId);

        try {
            switch (message.getMessageType()) {
                case CREATE_UPDATE:
                    updateWorkloadAtomic(message);
                    break;
                case DELETE:
                    deleteWorkload(message.getUsername(), message.getYear(), message.getMonth());
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getMessageType());
                    break;
            }

            logger.info("MongoDB workload operation completed for trainer: {}, transaction ID: {}",
                    message.getUsername(), transactionId);
        } catch (Exception e) {
            logger.error("Error processing MongoDB workload: {}, transaction ID: {}",
                    e.getMessage(), transactionId, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    // In WorkloadService.java
    public boolean workloadExists(String username, Integer year, Integer month) {
        // Implement logic to check if workload exists
        // This depends on your data access layer
        try {
            // Example implementation (adjust according to your repository structure)
            return workloadRepository.findByUsernameAndYearAndMonth(username, year, month) != null;
        } catch (Exception e) {
            logger.error("Error checking if workload exists for trainer: {}, period: {}/{}", username, year, month, e);
            return false;
        }
    }

    /**
     * Updates the workload for a trainer atomically using the MongoDB update operation.
     * This method is now corrected to properly update existing month records.
     */
    public void updateWorkloadAtomic(WorkloadMessage message) {
        String transactionId = message.getTransactionId();
        MDC.put("transactionId", transactionId);

        logger.info("MongoDB: Processing atomic workload update for trainer: {}, period: {}/{}",
                message.getUsername(), message.getYear(), message.getMonth());

        try {
            // Check if trainer exists
            boolean trainerExists = workloadRepository.existsById(message.getUsername());

            if (!trainerExists) {
                // Create new trainer document
                logger.info("MongoDB: Trainer not found, creating new record for: {}", message.getUsername());
                createNewTrainerWorkload(message);
                return;
            }

            // Fetch the entire trainer document first
            Optional<TrainerWorkloadDocument> trainerOpt = workloadRepository.findById(message.getUsername());

            if (trainerOpt.isPresent()) {
                TrainerWorkloadDocument trainer = trainerOpt.get();

                // Update basic trainer info
                trainer.setFirstName(message.getFirstName());
                trainer.setLastName(message.getLastName());
                trainer.setActive(message.isActive());

                // Find or create the year
                TrainerWorkloadDocument.YearSummary targetYear = null;
                for (TrainerWorkloadDocument.YearSummary yearSummary : trainer.getYears()) {
                    if (yearSummary.getYear() == message.getYear()) {
                        targetYear = yearSummary;
                        break;
                    }
                }

                // If year doesn't exist, create it
                if (targetYear == null) {
                    targetYear = new TrainerWorkloadDocument.YearSummary();
                    targetYear.setYear(message.getYear());
                    targetYear.setMonths(new ArrayList<>());
                    trainer.getYears().add(targetYear);
                }

                // Find or create the month
                TrainerWorkloadDocument.MonthSummary targetMonth = null;
                for (TrainerWorkloadDocument.MonthSummary monthSummary : targetYear.getMonths()) {
                    if (monthSummary.getMonth() == message.getMonth()) {
                        targetMonth = monthSummary;
                        break;
                    }
                }

                // If month doesn't exist, create it
                if (targetMonth == null) {
                    targetMonth = new TrainerWorkloadDocument.MonthSummary();
                    targetMonth.setMonth(message.getMonth());
                    targetYear.getMonths().add(targetMonth);
                }

                // Update the training duration
                targetMonth.setTrainingsSummaryDuration(message.getTrainingDuration());

                // Save the entire updated document
                workloadRepository.save(trainer);
                logger.debug("MongoDB: Updated trainer workload document: {}", trainer.getUsername());
            } else {
                // This shouldn't happen since we checked existsById, but just in case
                logger.warn("MongoDB: Trainer not found after existsById check: {}", message.getUsername());
                createNewTrainerWorkload(message);
            }
        } catch (Exception e) {
            logger.error("MongoDB: Error processing atomic workload update: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Create new trainer workload document
     */
    private void createNewTrainerWorkload(WorkloadMessage message) {
        TrainerWorkloadDocument trainer = new TrainerWorkloadDocument();
        trainer.setUsername(message.getUsername());
        trainer.setFirstName(message.getFirstName());
        trainer.setLastName(message.getLastName());
        trainer.setActive(message.isActive());

        // Create year summary
        TrainerWorkloadDocument.YearSummary yearSummary = new TrainerWorkloadDocument.YearSummary();
        yearSummary.setYear(message.getYear());

        // Create month summary
        TrainerWorkloadDocument.MonthSummary monthSummary = new TrainerWorkloadDocument.MonthSummary();
        monthSummary.setMonth(message.getMonth());
        monthSummary.setTrainingsSummaryDuration(message.getTrainingDuration());

        // Add month to year
        yearSummary.getMonths().add(monthSummary);

        // Add year to trainer
        trainer.getYears().add(yearSummary);

        // Save to MongoDB
        workloadRepository.save(trainer);
        logger.debug("MongoDB: Created new trainer workload document: {}", trainer.getUsername());
    }

    /**
     * Delete a specific month's workload
     */
    public void deleteWorkload(String username, int year, int month) {
        String transactionId = MDC.get("transactionId");
        if (transactionId == null) {
            transactionId = "no-transaction-id";
        }

        logger.info("MongoDB: Deleting workload for trainer: {}, period: {}/{}",
                username, year, month);

        // Check if trainer exists
        boolean trainerExists = workloadRepository.existsById(username);
        if (!trainerExists) {
            logger.warn("MongoDB: Trainer not found for deletion: {}", username);
            throw new ResourceNotFoundException("Trainer not found: " + username);
        }

        // Create query to find the specific month
        Query query = new Query(Criteria.where("username").is(username)
                .and("years.year").is(year)
                .and("years.months.month").is(month));

        // Check if the specific month exists
        boolean monthExists = mongoTemplate.exists(query, TrainerWorkloadDocument.class);
        if (!monthExists) {
            logger.warn("MongoDB: Month not found for deletion: {}/{} for trainer: {}",
                    year, month, username);
            throw new ResourceNotFoundException(
                    "Workload not found for trainer: " + username +
                            " for period: " + year + "/" + month);
        }

        // Find the document to remove the month
        TrainerWorkloadDocument trainer = workloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        // Find the year
        TrainerWorkloadDocument.YearSummary yearSummary = null;
        for (TrainerWorkloadDocument.YearSummary ys : trainer.getYears()) {
            if (ys.getYear() == year) {
                yearSummary = ys;
                break;
            }
        }

        if (yearSummary == null) {
            logger.warn("MongoDB: Year not found for deletion: {} for trainer: {}",
                    year, username);
            throw new ResourceNotFoundException(
                    "Year not found for trainer: " + username + " for year: " + year);
        }

        // Find and remove the month
        yearSummary.getMonths().removeIf(m -> m.getMonth() == month);

        // If year is now empty, remove it
        if (yearSummary.getMonths().isEmpty()) {
            trainer.getYears().remove(yearSummary);
        }

        // Save the updated document
        workloadRepository.save(trainer);
        logger.debug("MongoDB: Deleted workload for trainer: {}, period: {}/{}",
                username, year, month);
    }

    /**
     * Get trainer workload document by username
     */
    public TrainerWorkloadDocument getTrainerWorkload(String username) {
        String transactionId = MDC.get("transactionId");
        if (transactionId == null) {
            transactionId = "no-transaction-id";
        }

        logger.info("MongoDB: Getting workload for trainer: {}", username);

        return workloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));
    }

    /**
     * Find trainers by first name and last name
     */
    public List<TrainerWorkloadDocument> findTrainersByFullName(String firstName, String lastName) {
        String transactionId = MDC.get("transactionId");
        if (transactionId == null) {
            transactionId = "no-transaction-id";
        }

        logger.info("MongoDB: Finding trainers by name: {} {}", firstName, lastName);

        return workloadRepository.findByFirstNameAndLastName(firstName, lastName);
    }


    /**
     * Updates or creates a workload record
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @param firstName The trainer's first name
     * @param lastName The trainer's last name
     * @param active Whether the trainer is active
     * @param trainingDuration The training duration
     */
    public void updateOrCreateWorkload(
            String username,
            int year,
            int month,
            String firstName,
            String lastName,
            boolean active,
            int trainingDuration) {

        logger.info("Updating or creating workload for trainer: {}, period: {}/{}",
                username, year, month);

        // Create a WorkloadMessage to reuse existing logic
        WorkloadMessage message = new WorkloadMessage();
        message.setUsername(username);
        message.setYear(year);
        message.setMonth(month);
        message.setFirstName(firstName);
        message.setLastName(lastName);
        message.setActive(active);
        message.setTrainingDuration(trainingDuration);
        message.setMessageType(WorkloadMessage.MessageType.CREATE_UPDATE);
        message.setTransactionId(MDC.get("transactionId"));

        // Use the existing method for MongoDB updates
        updateWorkloadAtomic(message);


    }
}