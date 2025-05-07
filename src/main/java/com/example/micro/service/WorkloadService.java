package com.example.micro.service;


import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadUpdateRequest;
import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.repository.MonthSummaryRepository;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.repository.YearSummaryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
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
     * Update a trainer's workload based on a training session added or deleted
     */
    @Transactional
    public void updateTrainerWorkload(WorkloadUpdateRequest request) {
        String transactionId = MDC.get("transactionId");
        logger.info("Transaction ID: {} - Processing workload update for trainer: {}",
                transactionId, request.getUsername());

        // Get year and month from the training date
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        // Get or create trainer workload record
        TrainerWorkload trainerWorkload = trainerWorkloadRepository
                .findById(request.getUsername())
                .orElseGet(() -> {
                    TrainerWorkload newTrainer = new TrainerWorkload();
                    newTrainer.setUsername(request.getUsername());
                    newTrainer.setFirstName(request.getFirstName());
                    newTrainer.setLastName(request.getLastName());
                    newTrainer.setActive(request.isActive());
                    return newTrainer;
                });

        // Update trainer details in case they changed
        trainerWorkload.setFirstName(request.getFirstName());
        trainerWorkload.setLastName(request.getLastName());
        trainerWorkload.setActive(request.isActive());

        // Get or create year summary
        YearSummary yearSummary = trainerWorkload.getOrCreateYear(year);

        // Get or create month summary
        MonthSummary monthSummary = yearSummary.getOrCreateMonth(month);

        // Update summary duration based on action type
        if (request.getActionType() == WorkloadUpdateRequest.ActionType.ADD) {
            monthSummary.setSummaryDuration(monthSummary.getSummaryDuration() + request.getTrainingDuration());
            logger.info("Transaction ID: {} - Added {} minutes to trainer {}'s workload for {}/{}",
                    transactionId, request.getTrainingDuration(), request.getUsername(), year, month);
        } else if (request.getActionType() == WorkloadUpdateRequest.ActionType.DELETE) {
            // Subtract the duration, ensuring we don't go below zero
            int newDuration = Math.max(0, monthSummary.getSummaryDuration() - request.getTrainingDuration());
            monthSummary.setSummaryDuration(newDuration);
            logger.info("Transaction ID: {} - Removed {} minutes from trainer {}'s workload for {}/{}",
                    transactionId, request.getTrainingDuration(), request.getUsername(), year, month);
        }

        // Save everything
        trainerWorkloadRepository.save(trainerWorkload);
        logger.info("Transaction ID: {} - Successfully updated workload for trainer: {}",
                transactionId, request.getUsername());
    }

    /**
     * Get a trainer's monthly workload summary
     */
    public MonthlyWorkloadResponse getMonthlyWorkload(String username, int year, int month) {
        String transactionId = MDC.get("transactionId");
        logger.info("Transaction ID: {} - Retrieving monthly workload for trainer: {}, period: {}/{}",
                transactionId, username, year, month);

        Optional<MonthSummary> monthSummaryOpt = monthSummaryRepository
                .findByTrainerUsernameAndYearAndMonth(username, year, month);

        if (monthSummaryOpt.isEmpty()) {
            logger.info("Transaction ID: {} - No workload found for trainer: {}, period: {}/{}",
                    transactionId, username, year, month);
            return new MonthlyWorkloadResponse(username, "", "", false, year, month, 0);
        }

        MonthSummary monthSummary = monthSummaryOpt.get();
        TrainerWorkload trainer = monthSummary.getYearSummary().getTrainer();

        logger.info("Transaction ID: {} - Successfully retrieved workload for trainer: {}, period: {}/{}, duration: {} minutes",
                transactionId, username, year, month, monthSummary.getSummaryDuration());

        return new MonthlyWorkloadResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                year,
                month,
                monthSummary.getSummaryDuration()
        );
    }

    /**
     * Get the complete workload summary for a trainer
     */
    public TrainerWorkloadResponse getTrainerWorkloadSummary(String username) {
        String transactionId = MDC.get("transactionId");
        logger.info("Transaction ID: {} - Retrieving complete workload summary for trainer: {}",
                transactionId, username);

        Optional<TrainerWorkload> trainerOpt = trainerWorkloadRepository.findById(username);

        if (trainerOpt.isEmpty()) {
            logger.info("Transaction ID: {} - No workload data found for trainer: {}",
                    transactionId, username);
            return new TrainerWorkloadResponse(username, "", "", false);
        }

        TrainerWorkload trainer = trainerOpt.get();

        // Convert the entity to DTO
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setUsername(trainer.getUsername());
        response.setFirstName(trainer.getFirstName());
        response.setLastName(trainer.getLastName());
        response.setActive(trainer.isActive());

        // Map years and months
        response.setYears(trainer.getYears().stream()
                .map(year -> {
                    TrainerWorkloadResponse.YearSummaryDto yearDto =
                            new TrainerWorkloadResponse.YearSummaryDto();
                    yearDto.setYear(year.getYear());

                    // Map months
                    yearDto.setMonths(year.getMonths().stream()
                            .map(month -> new TrainerWorkloadResponse.MonthSummaryDto(
                                    month.getMonth(),
                                    month.getSummaryDuration()))
                            .collect(Collectors.toList()));

                    return yearDto;
                })
                .collect(Collectors.toList()));

        logger.info("Transaction ID: {} - Successfully retrieved complete workload summary for trainer: {}",
                transactionId, username);

        return response;
    }
}