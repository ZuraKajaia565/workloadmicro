package com.example.micro.repository;

import com.example.micro.document.TrainerWorkloadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadDocument, String> {

    // Find by firstName and lastName
    List<TrainerWorkloadDocument> findByFirstNameAndLastName(String firstName, String lastName);

    // Use MongoDB Query to find by nested properties
    @Query("{ 'username': ?0, 'years.year': ?1, 'years.months.month': ?2 }")
    Optional<TrainerWorkloadDocument> findByUsernameAndYearAndMonth(String username, Integer year, Integer month);

    // Check if a workload exists with MongoDB Query
    @Query(value = "{ 'username': ?0, 'years.year': ?1, 'years.months.month': ?2 }", exists = true)
    boolean existsByUsernameAndYearAndMonth(String username, Integer year, Integer month);
}