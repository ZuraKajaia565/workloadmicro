package com.example.micro.repository;

import com.example.micro.document.TrainerWorkloadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadDocument, String> {

    // Find by firstName and lastName if you need this method
    List<TrainerWorkloadDocument> findByFirstNameAndLastName(String firstName, String lastName);

    // Correct method declaration with Optional return type
    Optional<TrainerWorkloadDocument> findByUsernameAndYearAndMonth(String username, Integer year, Integer month);

    // Method to check if a workload exists
    boolean existsByUsernameAndYearAndMonth(String username, Integer year, Integer month);
}