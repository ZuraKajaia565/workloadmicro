package com.example.micro.controller;

import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadRequest;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.service.WorkloadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workloads")
public class WorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadController.class);

    private final WorkloadService workloadService;

    @Autowired
    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    /**
     * Create a workload entry for a trainer
     */
    @PostMapping("/trainers/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createTrainerWorkload(
            @PathVariable String username,
            @Valid @RequestBody WorkloadRequest request) {

        logger.info("Received workload creation request for trainer: {}", username);

        workloadService.createTrainerWorkload(username, request);

        logger.info("Successfully created workload for trainer: {}", username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update a workload entry for a trainer
     */
    @PutMapping("/trainers/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateTrainerWorkload(
            @PathVariable String username,
            @Valid @RequestBody WorkloadRequest request) {

        logger.info("Received workload update request for trainer: {}", username);

        workloadService.updateTrainerWorkload(username, request);

        logger.info("Successfully updated workload for trainer: {}", username);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a workload entry for a trainer
     */
    @DeleteMapping("/trainers/{username}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTrainerWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {

        logger.info("Received workload deletion request for trainer: {} for period: {}/{}",
                username, year, month);

        workloadService.deleteTrainerWorkload(username, year, month);

        logger.info("Successfully deleted workload for trainer: {} for period: {}/{}",
                username, year, month);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get a trainer's monthly workload
     */
    @GetMapping("/trainers/{username}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlyWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {

        logger.info("Retrieving monthly workload for trainer: {}, period: {}/{}",
                username, year, month);

        MonthSummary monthSummary = workloadService.getMonthlyWorkload(username, year, month);
        MonthlyWorkloadResponse response = convertToMonthlyResponse(monthSummary);

        logger.info("Successfully retrieved monthly workload for trainer: {}", username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a trainer's complete workload summary
     */
    @GetMapping("/trainers/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkloadSummary(
            @PathVariable String username) {

        logger.info("Retrieving complete workload summary for trainer: {}", username);

        TrainerWorkload trainerWorkload = workloadService.getTrainerWorkloadSummary(username);
        TrainerWorkloadResponse response = convertToSummaryResponse(trainerWorkload);

        logger.info("Successfully retrieved workload summary for trainer: {}", username);
        return ResponseEntity.ok(response);
    }

    /**
     * Converts a MonthSummary entity to a MonthlyWorkloadResponse DTO
     */
    private MonthlyWorkloadResponse convertToMonthlyResponse(MonthSummary monthSummary) {
        YearSummary yearSummary = monthSummary.getYearSummary();
        TrainerWorkload trainer = workloadService.getTrainerById(yearSummary.getTrainerUsername());

        return new MonthlyWorkloadResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                yearSummary.getYear(),
                monthSummary.getMonth(),
                monthSummary.getSummaryDuration()
        );
    }

    /**
     * Converts a TrainerWorkload entity to a TrainerWorkloadResponse DTO
     */
    private TrainerWorkloadResponse convertToSummaryResponse(TrainerWorkload trainerWorkload) {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setUsername(trainerWorkload.getUsername());
        response.setFirstName(trainerWorkload.getFirstName());
        response.setLastName(trainerWorkload.getLastName());
        response.setActive(trainerWorkload.isActive());

        // Convert years
        response.setYears(trainerWorkload.getYears().stream()
                .map(this::convertYearSummary)
                .collect(Collectors.toList()));

        return response;
    }

    private TrainerWorkloadResponse.YearSummaryDto convertYearSummary(YearSummary yearSummary) {
        TrainerWorkloadResponse.YearSummaryDto yearDto = new TrainerWorkloadResponse.YearSummaryDto(yearSummary.getYear());

        // Convert months
        yearDto.setMonths(yearSummary.getMonths().stream()
                .map(month -> new TrainerWorkloadResponse.MonthSummaryDto(
                        month.getMonth(),
                        month.getSummaryDuration()))
                .collect(Collectors.toList()));

        return yearDto;
    }
}