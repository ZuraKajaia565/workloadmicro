package com.example.micro.controller;

import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadRequest;
import com.example.micro.exception.InsufficientWorkloadException;
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
@RequestMapping("/api/trainers/{username}/workloads")
public class WorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadController.class);

    private final WorkloadService workloadService;

    @Autowired
    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainerWorkloadResponse>> getAllWorkloads(
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {

        logger.info("Retrieving all workloads");
        List<TrainerWorkloadResponse> workloads = workloadService.getAllTrainerWorkloads();
        return ResponseEntity.ok(workloads);
    }

    /**
     * Create or update workload for a specific month
     */
    @PutMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month,
            @Valid @RequestBody WorkloadRequest request) {

        logger.info("Updating workload for trainer: {}, period: {}/{}", username, year, month);

        try {
            workloadService.updateOrCreateWorkload(username, year, month, request.getFirstName(),
                    request.getLastName(), request.isActive(), request.getTrainingDuration());

            logger.info("Successfully updated workload for trainer: {}, period: {}/{}",
                    username, year, month);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get workload for a specific month
     */
    @GetMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlyWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {

        logger.info("Retrieving monthly workload for trainer: {}, period: {}/{}",
                username, year, month);

        try {
            MonthSummary monthSummary = workloadService.getMonthlyWorkload(username, year, month);
            MonthlyWorkloadResponse response = convertToMonthlyResponse(monthSummary);

            logger.info("Successfully retrieved monthly workload for trainer: {}", username);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Workload not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete workload for a specific month
     */
    @DeleteMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {

        logger.info("Deleting workload for trainer: {}, period: {}/{}",
                username, year, month);

        try {
            workloadService.deleteWorkload(username, year, month);

            logger.info("Successfully deleted workload for trainer: {}, period: {}/{}",
                    username, year, month);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Workload not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add training duration to a trainer's workload
     */
    @PostMapping("/{year}/{month}/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam int duration) {

        logger.info("Adding {} minutes to workload for trainer: {}, period: {}/{}",
                duration, username, year, month);

        try {
            workloadService.addWorkload(username, year, month, duration);

            logger.info("Successfully added duration to workload for trainer: {}", username);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Workload not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Subtract training duration from a trainer's workload
     */
    @PostMapping("/{year}/{month}/subtract")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> subtractWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam int duration) {

        logger.info("Subtracting {} minutes from workload for trainer: {}, period: {}/{}",
                duration, username, year, month);

        try {
            workloadService.subtractWorkload(username, year, month, duration);

            logger.info("Successfully subtracted duration from workload for trainer: {}", username);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Workload not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InsufficientWorkloadException e) {
            logger.warn("Insufficient workload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get complete workload summary
     */

    public ResponseEntity<TrainerWorkloadResponse> getWorkloadSummary(
            @PathVariable String username) {

        logger.info("Retrieving complete workload summary for trainer: {}", username);

        try {
            TrainerWorkload trainerWorkload = workloadService.getTrainerWorkloadSummary(username);
            TrainerWorkloadResponse response = convertToSummaryResponse(trainerWorkload);

            logger.info("Successfully retrieved workload summary for trainer: {}", username);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Trainer not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
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