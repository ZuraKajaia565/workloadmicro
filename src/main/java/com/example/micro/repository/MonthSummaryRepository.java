package com.example.micro.repository;

import com.example.micro.model.MonthSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for month summary data
 */
@Repository
public interface MonthSummaryRepository extends JpaRepository<MonthSummary, Long> {

    @Query("SELECT ms FROM MonthSummary ms " +
            "JOIN ms.yearSummary ys " +
            "WHERE ys.trainerUsername = :username " +
            "AND ys.year = :year " +
            "AND ms.month = :month")
    Optional<MonthSummary> findByTrainerUsernameAndYearAndMonth(
            @Param("username") String username,
            @Param("year") int year,
            @Param("month") int month);
}