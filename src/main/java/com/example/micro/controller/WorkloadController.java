package com.example.micro.controller;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadRequest;
import com.example.micro.exception.InsufficientWorkloadException;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers/{username}/workloads")
public class WorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadController.class);

    private final WorkloadService workloadService;
    private final TrainerWorkloadRepository workloadRepository;

    @Autowired
    public WorkloadController(WorkloadService workloadService, TrainerWorkloadRepository workloadRepository) {
        this.workloadService = workloadService;
        this.workloadRepository = workloadRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTrainerWorkload(@PathVariable String username) {
        logger.info("Retrieving workload for trainer: {}", username);

        try {
            TrainerWorkloadDocument trainerWorkload = workloadService.getTrainerWorkload(username);
            return ResponseEntity.ok(trainerWorkload);
        } catch (ResourceNotFoundException e) {
            logger.warn("Trainer not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchTrainersByName(
            @RequestParam String firstName,
            @RequestParam String lastName) {

        logger.info("Searching trainers by name: {} {}", firstName, lastName);

        try {
            List<TrainerWorkloadDocument> trainers = workloadService.findTrainersByFullName(firstName, lastName);
            return ResponseEntity.ok(trainers);
        } catch (Exception e) {
            logger.error("Error searching trainers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }

    }

    @PutMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrUpdateWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month,
            @Valid @RequestBody WorkloadRequest request) {

        logger.info("Creating/updating workload for trainer: {}, period: {}/{}", username, year, month);

        try {
            workloadService.updateOrCreateWorkload(
                    username,
                    year,
                    month,
                    request.getFirstName(),
                    request.getLastName(),
                    request.isActive(),
                    request.getTrainingDuration()
            );

            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Trainer not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error creating/updating workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {

        logger.info("Deleting workload for trainer: {}, period: {}/{}", username, year, month);

        try {
            workloadService.deleteWorkload(username, year, month);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMonthlyWorkload(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {

        logger.info("Retrieving monthly workload for trainer: {}, period: {}/{}",
                username, year, month);

        try {
            // This is just an example - implement the actual method based on your needs
            Optional<TrainerWorkloadDocument> trainerOpt = workloadRepository.findById(username);

            if (trainerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TrainerWorkloadDocument trainer = trainerOpt.get();

            // Find the year and month
            for (TrainerWorkloadDocument.YearSummary yearSummary : trainer.getYears()) {
                if (yearSummary.getYear() == year) {
                    for (TrainerWorkloadDocument.MonthSummary monthSummary : yearSummary.getMonths()) {
                        if (monthSummary.getMonth() == month) {
                            // Create and return response
                            Map<String, Object> response = new HashMap<>();
                            response.put("username", username);
                            response.put("year", year);
                            response.put("month", month);
                            response.put("minutes", monthSummary.getTrainingsSummaryDuration());

                            return ResponseEntity.ok(response);
                        }
                    }
                }
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving monthly workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}