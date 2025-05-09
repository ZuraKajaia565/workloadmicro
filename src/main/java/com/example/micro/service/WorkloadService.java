package com.example.micro.service;

import com.example.micro.dto.TrainerWorkloadResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Update or create a workload entry for a trainer
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @param firstName The trainer's first name
     * @param lastName The trainer's last name
     * @param isActive The trainer's active status
     * @param duration The workload duration
     * @return The updated trainer workload
     */
    @Transactional
    public TrainerWorkload updateOrCreateWorkload(String username, int year, int month,
                                                  String firstName, String lastName,
                                                  boolean isActive, int duration) {

        logger.debug("Updating or creating workload for trainer: {}, period: {}/{}",
                username, year, month);

        // Find or create trainer
        TrainerWorkload trainer = trainerWorkloadRepository.findById(username)
                .orElseGet(() -> {
                    TrainerWorkload newTrainer = new TrainerWorkload();
                    newTrainer.setUsername(username);
                    newTrainer.setYears(new ArrayList<>());
                    return newTrainer;
                });

        // Update trainer details
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setActive(isActive);

        // Find or create year
        YearSummary yearSummary = trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary();
                    newYear.setYear(year);
                    newYear.setTrainerUsername(username);
                    newYear.setMonths(new ArrayList<>());
                    trainer.getYears().add(newYear);
                    return newYear;
                });

        // Find or create month
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setYearId(yearSummary.getId());
                    newMonth.setSummaryDuration(0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        // Set the duration
        monthSummary.setSummaryDuration(duration);

        // Save and return
        return trainerWorkloadRepository.save(trainer);
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
        logger.debug("Getting monthly workload for trainer: {}, period: {}/{}",
                username, year, month);

        return monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(username, year, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workload not found for trainer: " + username +
                                " for period: " + year + "/" + month));
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
    public void deleteWorkload(String username, int year, int month) {
        logger.debug("Deleting workload for trainer: {}, period: {}/{}",
                username, year, month);

        // Find trainer
        TrainerWorkload trainer = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        // Find year
        YearSummary yearSummary = trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workload not found for trainer: " + username + " for year: " + year));

        // Find month
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workload not found for trainer: " + username +
                                " for period: " + year + "/" + month));

        // Remove month
        yearSummary.getMonths().remove(monthSummary);

        // If year is now empty, remove it too
        if (yearSummary.getMonths().isEmpty()) {
            trainer.getYears().remove(yearSummary);
        }

        // Save
        trainerWorkloadRepository.save(trainer);
    }

    /**
     * Add to a trainer's workload for a specific month
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @param duration The duration to add in minutes
     * @return The updated trainer workload
     * @throws ResourceNotFoundException if the trainer doesn't exist
     */
    @Transactional
    public TrainerWorkload addWorkload(String username, int year, int month, int duration) {
        logger.debug("Adding {} minutes to workload for trainer: {}, period: {}/{}",
                duration, username, year, month);

        // Find trainer
        TrainerWorkload trainer = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        // Find or create year
        YearSummary yearSummary = trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary();
                    newYear.setYear(year);
                    newYear.setTrainerUsername(username);
                    newYear.setMonths(new ArrayList<>());
                    trainer.getYears().add(newYear);
                    return newYear;
                });

        // Find or create month
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    newMonth.setYearId(yearSummary.getId());
                    newMonth.setSummaryDuration(0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        // Add duration
        int currentDuration = monthSummary.getSummaryDuration();
        int newDuration = currentDuration + duration;
        monthSummary.setSummaryDuration(newDuration);

        // Save and return
        return trainerWorkloadRepository.save(trainer);
    }

    /**
     * Subtract from a trainer's workload for a specific month
     *
     * @param username The trainer's username
     * @param year The year
     * @param month The month
     * @param duration The duration to subtract in minutes
     * @return The updated trainer workload
     * @throws ResourceNotFoundException if the trainer or workload entry doesn't exist
     * @throws InsufficientWorkloadException if the remaining workload would be negative
     */
    @Transactional
    public TrainerWorkload subtractWorkload(String username, int year, int month, int duration) {
        logger.debug("Subtracting {} minutes from workload for trainer: {}, period: {}/{}",
                duration, username, year, month);

        // Find trainer
        TrainerWorkload trainer = trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        // Find year
        YearSummary yearSummary = trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workload not found for trainer: " + username + " for year: " + year));

        // Find month
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workload not found for trainer: " + username +
                                " for period: " + year + "/" + month));

        // Check if there's enough duration to subtract
        int currentDuration = monthSummary.getSummaryDuration();
        if (currentDuration < duration) {
            throw new InsufficientWorkloadException(
                    String.format("Cannot subtract %d minutes from current workload of %d minutes for trainer: %s for period: %d/%d",
                            duration, currentDuration, username, year, month));
        }

        // Subtract duration
        int newDuration = currentDuration - duration;
        monthSummary.setSummaryDuration(newDuration);

        // If duration becomes zero, consider removing the month
        if (newDuration == 0) {
            yearSummary.getMonths().remove(monthSummary);

            // If year is now empty, remove it too
            if (yearSummary.getMonths().isEmpty()) {
                trainer.getYears().remove(yearSummary);
            }
        }

        // Save and return
        return trainerWorkloadRepository.save(trainer);
    }

    public List<TrainerWorkloadResponse> getAllTrainerWorkloads() {
        logger.info("Getting all trainer workloads");

        // Get all trainers from repository
        List<TrainerWorkload> trainers = trainerWorkloadRepository.findAll();
        logger.info("Found {} trainers in database", trainers.size());

        if (trainers.isEmpty()) {
            logger.warn("No trainers found in database");
            return new ArrayList<>();
        }

        // Log details about each trainer
        for (TrainerWorkload trainer : trainers) {
            logger.info("Trainer: {}, First Name: {}, Last Name: {}, Active: {}, Years: {}",
                    trainer.getUsername(),
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.isActive(),
                    trainer.getYears().size());
        }

        // Convert to response objects
        List<TrainerWorkloadResponse> responses = trainers.stream()
                .map(this::convertToTrainerWorkloadResponse)
                .collect(Collectors.toList());

        logger.info("Returning {} trainer workload responses", responses.size());
        return responses;
    }

    private TrainerWorkloadResponse convertToTrainerWorkloadResponse(TrainerWorkload trainer) {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();

        response.setUsername(trainer.getUsername());
        response.setFirstName(trainer.getFirstName());
        response.setLastName(trainer.getLastName());
        response.setActive(trainer.isActive());

        // Convert years
        List<TrainerWorkloadResponse.YearSummaryDto> yearDtos = trainer.getYears().stream()
                .map(this::convertYearSummaryToDto)
                .collect(Collectors.toList());

        response.setYears(yearDtos);

        return response;
    }

    private TrainerWorkloadResponse.YearSummaryDto convertYearSummaryToDto(YearSummary year) {
        TrainerWorkloadResponse.YearSummaryDto yearDto = new TrainerWorkloadResponse.YearSummaryDto(year.getYear());

        // Convert months
        List<TrainerWorkloadResponse.MonthSummaryDto> monthDtos = year.getMonths().stream()
                .map(month -> convertMonthSummaryToDto(month))
                .collect(Collectors.toList());

        yearDto.setMonths(monthDtos);

        return yearDto;
    }

    private TrainerWorkloadResponse.MonthSummaryDto convertMonthSummaryToDto(MonthSummary month) {
        return new TrainerWorkloadResponse.MonthSummaryDto(
                month.getMonth(),
                month.getSummaryDuration()
        );
    }

    /**
     * Get the complete workload summary for a trainer
     *
     * @param username The trainer's username
     * @return The trainer workload entity with all workload data
     * @throws ResourceNotFoundException if the trainer is not found
     */
    public TrainerWorkload getTrainerWorkloadSummary(String username) {
        logger.debug("Getting workload summary for trainer: {}", username);

        return trainerWorkloadRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));
    }
}