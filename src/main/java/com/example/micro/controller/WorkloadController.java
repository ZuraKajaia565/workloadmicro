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
}