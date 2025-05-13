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


    private final WorkloadService workloadMongoService;

    @Autowired
    public WorkloadController(WorkloadService workloadMongoService) {
        this.workloadMongoService = workloadMongoService;
    }

    // Your existing controller methods

    @GetMapping("/mongo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMongoWorkload(@PathVariable String username) {
        logger.info("Retrieving MongoDB workload for trainer: {}", username);

        try {
            TrainerWorkloadDocument trainerWorkload = workloadMongoService.getTrainerWorkload(username);
            return ResponseEntity.ok(trainerWorkload);
        } catch (ResourceNotFoundException e) {
            logger.warn("MongoDB trainer not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving MongoDB workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/mongo/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchMongoTrainers(
            @RequestParam String firstName,
            @RequestParam String lastName) {

        logger.info("Searching MongoDB trainers by name: {} {}", firstName, lastName);

        try {
            List<TrainerWorkloadDocument> trainers = workloadMongoService.findTrainersByFullName(firstName, lastName);
            return ResponseEntity.ok(trainers);
        } catch (Exception e) {
            logger.error("Error searching MongoDB trainers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}