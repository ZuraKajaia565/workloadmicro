package com.example.micro.service;

import com.example.micro.dto.WorkloadRequest;
import com.example.micro.exception.InsufficientWorkloadException;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.repository.MonthSummaryRepository;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.repository.YearSummaryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for handling trainer workload operations
 */
@Service
public class WorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadService.class);

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final YearSummaryRepository yearSummaryRepository;
    private final MonthSummaryRepository monthSummaryRepository;

    @Autowired
    public WorkloadService(
            TrainerWorkloadRepository trainerWorkloadRepository,
            YearSummaryRepository yearSummaryRepository,
            MonthSummaryRepository monthSummaryRepository) {
        this.trainerWorkloadRepository = trainerWorkloadRepository;
        this.yearSummaryRepository = yearSummaryRepository;
        this.monthSummaryRepository = monthSummaryRepository;
    }

    /**
     * Check if a trainer exists
     *
     * @param username The trainer's username
     * @return true if the trainer exists, false otherwise
     */
    public boolean trainerExists(String username) {
        return trainerWorkloadRepository.existsById(username);
    }

    /**
     * Get a trainer by username
     *
     * @param username The trainer's username
     * @return The trainer workload entity
     * @throws ResourceNotFoundException if the trainer is not found
     */
    public TrainerWorkload getTrainerById(String username) {
        return trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));
    }

    /**
     * Create a new workload entry for a trainer
     *
     * @param username The trainer's username
     * @param request The workload request data
     */
    @Transactional
    public void createTrainerWorkload(String username, WorkloadRequest request) {
        logger.info("Creating workload for trainer: {}", username);

        // Use the username from the path parameter
        TrainerWorkload trainerWorkload = trainerWorkloadRepository
                .findById(username)
                .orElseGet(() -> {
                    logger.info("Trainer not found, creating new trainer record: {}", username);
                    TrainerWorkload newTrainer = new TrainerWorkload();
                    newTrainer.setUsername(username);
                    newTrainer.setFirstName(request.getFirstName());
                    newTrainer.setLastName(request.getLastName());
                    newTrainer.setActive(request.isActive());
                    return newTrainer;
                });

        // Update trainer details in case they changed
        trainerWorkload.setFirstName(request.getFirstName());
        trainerWorkload.setLastName(request.getLastName());
        trainerWorkload.setActive(request.isActive());

        // Get year and month from the training date
        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();

        // Get or create year summary
        YearSummary yearSummary = trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new year summary for {}: {}", username, year);
                    YearSummary newYear = new YearSummary();
                    newYear.setYear(year);
                    newYear.setTrainerUsername(username);
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });

        // Get or create month summary
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new month summary for {}: {}/{}", username, year, month);
                    MonthSummary newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setYearId(yearSummary.getId());
                    newMonth.setSummaryDuration(0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        // Add the duration
        int currentDuration = monthSummary.getSummaryDuration();
        int newDuration = currentDuration + request.getTrainingDuration();
        monthSummary.setSummaryDuration(newDuration);

        // Save everything
        TrainerWorkload savedTrainer = trainerWorkloadRepository.save(trainerWorkload);

        logger.info("Created workload for trainer: {}, period: {}/{}, new total duration: {} minutes",
                username, year, month, newDuration);
    }

    /**
     * Update an existing workload entry for a trainer
     *
     * @param username The trainer's username
     * @param request The workload request data
     * @throws ResourceNotFoundException if the trainer doesn't exist
     */
    @Transactional
    public void updateTrainerWorkload(String username, WorkloadRequest request) {
        logger.info("Updating workload for trainer: {}", username);

        // Check if trainer exists
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> {
                    logger.error("Trainer not found: {}", username);
                    return new ResourceNotFoundException("Trainer not found: " + username);
                });

        // Update trainer details
        trainerWorkload.setFirstName(request.getFirstName());
        trainerWorkload.setLastName(request.getLastName());
        trainerWorkload.setActive(request.isActive());

        // Get year and month from the training date
        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();

        // Find or create the year summary
        YearSummary yearSummary = trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new year summary for {}: {}", username, year);
                    YearSummary newYear = new YearSummary();
                    newYear.setYear(year);
                    newYear.setTrainerUsername(username);
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });

        // Find or create the month summary
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new month summary for {}: {}/{}", username, year, month);
                    MonthSummary newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setYearId(yearSummary.getId());
                    newMonth.setSummaryDuration(0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        // Set the new duration directly (replacing the old value)
        monthSummary.setSummaryDuration(request.getTrainingDuration());

        // Save everything
        trainerWorkloadRepository.save(trainerWorkload);

        logger.info("Updated workload for trainer: {}, period: {}/{}, new duration: {} minutes",
                username, year, month, request.getTrainingDuration());
    }

    /**
     * Delete a workload entry for a trainer
     *
     * @param username The trainer's username
     * @param year The year to delete
     * @param month The month to delete
     * @throws ResourceNotFoundException if the trainer or workload entry doesn't exist
     */
    @Transactional
    public void deleteTrainerWorkload(String username, int year, int month) {
        logger.info("Deleting workload for trainer: {}, period: {}/{}", username, year, month);

        // Check if trainer exists
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> {
                    logger.error("Trainer not found: {}", username);
                    return new ResourceNotFoundException("Trainer not found: " + username);
                });

        // Find the year
        Optional<YearSummary> yearOpt = trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst();

        if (yearOpt.isEmpty()) {
            logger.error("Year not found for trainer: {}, year: {}", username, year);
            throw new ResourceNotFoundException("Workload not found for trainer: " + username +
                    " for year: " + year);
        }

        YearSummary yearSummary = yearOpt.get();

        // Find the month
        Optional<MonthSummary> monthOpt = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst();

        if (monthOpt.isEmpty()) {
            logger.error("Month not found for trainer: {}, period: {}/{}", username, year, month);
            throw new ResourceNotFoundException("Workload not found for trainer: " + username +
                    " for period: " + year + "/" + month);
        }

        // Remove the month from the year
        MonthSummary monthSummary = monthOpt.get();
        yearSummary.getMonths().remove(monthSummary);

        // If the year is now empty, remove it too
        if (yearSummary.getMonths().isEmpty()) {
            logger.debug("Removing empty year summary for {}: {}", username, year);
            trainerWorkload.getYears().remove(yearSummary);
        }

        // Save everything
        trainerWorkloadRepository.save(trainerWorkload);

        logger.info("Deleted workload for trainer: {}, period: {}/{}", username, year, month);
    }

    /**
     * Add to a trainer's workload for a specific month
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @param duration The duration to add in minutes
     * @throws ResourceNotFoundException if the trainer doesn't exist
     */
    @Transactional
    public void addTrainerWorkload(String username, int year, int month, int duration) {
        logger.info("Adding {} minutes to workload for trainer: {}, period: {}/{}",
                duration, username, year, month);

        // Check if trainer exists
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> {
                    logger.error("Trainer not found: {}", username);
                    return new ResourceNotFoundException("Trainer not found: " + username);
                });

        // Find or create the year summary
        YearSummary yearSummary = trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new year summary for {}: {}", username, year);
                    YearSummary newYear = new YearSummary();
                    newYear.setYear(year);
                    newYear.setTrainerUsername(username);
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });

        // Find or create the month summary
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    logger.debug("Creating new month summary for {}: {}/{}", username, year, month);
                    MonthSummary newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setYearId(yearSummary.getId());
                    newMonth.setSummaryDuration(0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        // Add the duration
        int currentDuration = monthSummary.getSummaryDuration();
        int newDuration = currentDuration + duration;
        monthSummary.setSummaryDuration(newDuration);

        // Save everything
        trainerWorkloadRepository.save(trainerWorkload);

        logger.info("Added {} minutes to workload for trainer: {}, period: {}/{}, new total: {} minutes",
                duration, username, year, month, newDuration);
    }

    /**
     * Subtract from a trainer's workload for a specific month
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @param duration The duration to subtract in minutes
     * @throws ResourceNotFoundException if the trainer or workload entry doesn't exist
     * @throws InsufficientWorkloadException if the remaining workload would be negative
     */
    @Transactional
    public void subtractTrainerWorkload(String username, int year, int month, int duration) {
        logger.info("Subtracting {} minutes from workload for trainer: {}, period: {}/{}",
                duration, username, year, month);

        // Check if trainer exists
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> {
                    logger.error("Trainer not found: {}", username);
                    return new ResourceNotFoundException("Trainer not found: " + username);
                });

        // Find the year
        Optional<YearSummary> yearOpt = trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst();

        if (yearOpt.isEmpty()) {
            logger.error("Year not found for trainer: {}, year: {}", username, year);
            throw new ResourceNotFoundException("Workload not found for trainer: " + username +
                    " for year: " + year);
        }

        YearSummary yearSummary = yearOpt.get();

        // Find the month
        Optional<MonthSummary> monthOpt = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst();

        if (monthOpt.isEmpty()) {
            logger.error("Month not found for trainer: {}, period: {}/{}", username, year, month);
            throw new ResourceNotFoundException("Workload not found for trainer: " + username +
                    " for period: " + year + "/" + month);
        }

        // Check if there's enough duration to subtract
        MonthSummary monthSummary = monthOpt.get();
        int currentDuration = monthSummary.getSummaryDuration();

        if (currentDuration < duration) {
            String errorMessage = String.format(
                    "Cannot subtract %d minutes from current workload of %d minutes for trainer: %s for period: %d/%d",
                    duration, currentDuration, username, year, month);
            logger.error(errorMessage);
            throw new InsufficientWorkloadException(errorMessage);
        }

        // Subtract the duration
        int newDuration = currentDuration - duration;
        monthSummary.setSummaryDuration(newDuration);

        // If duration becomes zero, consider removing the month
        if (newDuration == 0) {
            logger.debug("Workload is now zero, removing month summary for {}: {}/{}",
                    username, year, month);
            yearSummary.getMonths().remove(monthSummary);

            // If year is now empty, remove it too
            if (yearSummary.getMonths().isEmpty()) {
                logger.debug("Year is now empty, removing year summary for {}: {}",
                        username, year);
                trainerWorkload.getYears().remove(yearSummary);
            }
        }

        // Save everything
        trainerWorkloadRepository.save(trainerWorkload);

        logger.info("Subtracted {} minutes from workload for trainer: {}, period: {}/{}, new total: {} minutes",
                duration, username, year, month, newDuration);
    }

    /**
     * Get a trainer's monthly workload
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @return The month summary entity
     * @throws ResourceNotFoundException if the workload data is not found
     */
    public MonthSummary getMonthlyWorkload(String username, int year, int month) {
        logger.info("Retrieving monthly workload for trainer: {}, period: {}/{}",
                username, year, month);

        // Use the repository method to find the month summary
        Optional<MonthSummary> monthSummaryOpt = monthSummaryRepository
                .findByTrainerUsernameAndYearAndMonth(username, year, month);

        if (monthSummaryOpt.isEmpty()) {
            logger.error("Monthly workload not found for trainer: {}, period: {}/{}",
                    username, year, month);
            throw new ResourceNotFoundException("Workload not found for trainer: " + username +
                    " for period: " + year + "/" + month);
        }

        MonthSummary monthSummary = monthSummaryOpt.get();
        logger.info("Retrieved monthly workload for trainer: {}, period: {}/{}, duration: {} minutes",
                username, year, month, monthSummary.getSummaryDuration());

        return monthSummary;
    }

    /**
     * Get the complete workload summary for a trainer
     *
     * @param username The trainer's username
     * @return The trainer workload entity with all workload data
     * @throws ResourceNotFoundException if the trainer is not found
     */
    public TrainerWorkload getTrainerWorkloadSummary(String username) {
        logger.info("Retrieving complete workload summary for trainer: {}", username);

        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> {
                    logger.error("Trainer not found: {}", username);
                    return new ResourceNotFoundException("Trainer not found: " + username);
                });

        int totalYears = trainerWorkload.getYears().size();
        int totalMonths = trainerWorkload.getYears().stream()
                .mapToInt(y -> y.getMonths().size())
                .sum();

        logger.info("Retrieved workload summary for trainer: {}, years: {}, months: {}",
                username, totalYears, totalMonths);

        return trainerWorkload;
    }
}