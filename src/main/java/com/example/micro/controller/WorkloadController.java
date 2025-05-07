package com.example.micro.controller;


import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadUpdateRequest;
import com.example.micro.service.WorkloadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workload")
public class WorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadController.class);

    private final WorkloadService workloadService;

    @Autowired
    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    /**
     * Endpoint to update a trainer's workload
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateTrainerWorkload(
            @Valid @RequestBody WorkloadUpdateRequest request,
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {

        // If no transaction ID is provided, generate one
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        // Set the transaction ID in the MDC
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Transaction ID: {} - Received workload update request for trainer: {}",
                    transactionId, request.getUsername());

            workloadService.updateTrainerWorkload(request);

            logger.info("Transaction ID: {} - Successfully processed workload update for trainer: {}",
                    transactionId, request.getUsername());

            return ResponseEntity.ok("Workload updated successfully");
        } finally {
            MDC.clear();
        }
    }

    /**
     * Endpoint to get a trainer's monthly workload
     */
    @GetMapping("/monthly")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlyWorkload(
            @RequestParam String username,
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {

        // If no transaction ID is provided, generate one
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        // Set the transaction ID in the MDC
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Transaction ID: {} - Received monthly workload request for trainer: {}, period: {}/{}",
                    transactionId, username, year, month);

            MonthlyWorkloadResponse response = workloadService.getMonthlyWorkload(username, year, month);

            logger.info("Transaction ID: {} - Successfully retrieved monthly workload for trainer: {}",
                    transactionId, username);

            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Endpoint to get a trainer's complete workload summary
     */
    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkloadSummary(
            @PathVariable String username,
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {

        // If no transaction ID is provided, generate one
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        // Set the transaction ID in the MDC
        MDC.put("transactionId", transactionId);

        try {
            logger.info("Transaction ID: {} - Received complete workload summary request for trainer: {}",
                    transactionId, username);

            TrainerWorkloadResponse response = workloadService.getTrainerWorkloadSummary(username);

            logger.info("Transaction ID: {} - Successfully retrieved workload summary for trainer: {}",
                    transactionId, username);

            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }
}