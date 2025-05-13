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

    /**
     * Update workload using MongoDB's atomic operations
     * This is more efficient for large documents as it avoids fetching the entire document
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

            // Create query to find the specific month within the specific year
            Query query = new Query(Criteria.where("username").is(message.getUsername())
                    .and("years.year").is(message.getYear())
                    .and("years.months.month").is(message.getMonth()));

            // Check if the specific month exists
            boolean monthExists = mongoTemplate.exists(query, TrainerWorkloadDocument.class);

            if (monthExists) {
                // Update existing month duration
                logger.debug("MongoDB: Found existing month record for trainer: {}, updating duration", message.getUsername());

                // Create update operation for the specific month
                Update update = new Update()
                        .set("firstName", message.getFirstName())
                        .set("lastName", message.getLastName())
                        .set("isActive", message.isActive());

                // Depending on message type, either set or increment duration
                if (message.getMessageType() == WorkloadMessage.MessageType.CREATE_UPDATE) {
                    update.set("years.$[year].months.$[month].trainingsSummaryDuration", message.getTrainingDuration());
                } else {
                    update.inc("years.$[year].months.$[month].trainingsSummaryDuration", message.getTrainingDuration());
                }

                // Add array filters to target the specific year and month
                query = new Query(Criteria.where("username").is(message.getUsername()));
                mongoTemplate.updateFirst(
                        query,
                        update,
                        "trainer_workloads"
                );

                logger.debug("MongoDB: Updated workload duration atomically for trainer: {}, period: {}/{}",
                        message.getUsername(), message.getYear(), message.getMonth());
            } else {
                // Month doesn't exist, so we need more complex handling
                // First check if year exists
                query = new Query(Criteria.where("username").is(message.getUsername())
                        .and("years.year").is(message.getYear()));

                boolean yearExists = mongoTemplate.exists(query, TrainerWorkloadDocument.class);

                if (yearExists) {
                    // Year exists, but month doesn't - add new month to existing year
                    logger.debug("MongoDB: Year exists but month doesn't for trainer: {}, adding new month", message.getUsername());

                    TrainerWorkloadDocument.MonthSummary newMonth = new TrainerWorkloadDocument.MonthSummary();
                    newMonth.setMonth(message.getMonth());
                    newMonth.setTrainingsSummaryDuration(message.getTrainingDuration());

                    Update update = new Update().push("years.$[year].months", newMonth);

                    mongoTemplate.updateFirst(
                            new Query(Criteria.where("username").is(message.getUsername())),
                            update,
                            "trainer_workloads"
                    );
                } else {
                    // Neither year nor month exists - add new year with new month
                    logger.debug("MongoDB: Neither year nor month exists for trainer: {}, adding both", message.getUsername());

                    TrainerWorkloadDocument.MonthSummary newMonth = new TrainerWorkloadDocument.MonthSummary();
                    newMonth.setMonth(message.getMonth());
                    newMonth.setTrainingsSummaryDuration(message.getTrainingDuration());

                    TrainerWorkloadDocument.YearSummary newYear = new TrainerWorkloadDocument.YearSummary();
                    newYear.setYear(message.getYear());
                    newYear.getMonths().add(newMonth);

                    Update update = new Update()
                            .set("firstName", message.getFirstName())
                            .set("lastName", message.getLastName())
                            .set("isActive", message.isActive())
                            .push("years", newYear);

                    mongoTemplate.updateFirst(
                            new Query(Criteria.where("username").is(message.getUsername())),
                            update,
                            "trainer_workloads"
                    );
                }

                logger.debug("MongoDB: Added new workload record for trainer: {}, period: {}/{}",
                        message.getUsername(), message.getYear(), message.getMonth());
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