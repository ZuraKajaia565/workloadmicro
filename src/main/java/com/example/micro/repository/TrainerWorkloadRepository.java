package com.example.micro.repository;

import com.example.micro.model.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for trainer workload data
 */
@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {
    // Standard JpaRepository methods are sufficient
}