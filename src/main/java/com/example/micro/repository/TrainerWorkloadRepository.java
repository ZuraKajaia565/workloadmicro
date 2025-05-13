package com.example.micro.repository;

import com.example.micro.document.TrainerWorkloadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadDocument, String> {

    // Find by firstName and lastName (will use the compound index)
    List<TrainerWorkloadDocument> findByFirstNameAndLastName(String firstName, String lastName);
}