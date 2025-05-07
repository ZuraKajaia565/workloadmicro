package com.example.micro.repository;

import com.example.micro.model.YearSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YearSummaryRepository extends JpaRepository<YearSummary, Long> {
    @Query("SELECT ys FROM YearSummary ys WHERE ys.trainer.username = :username AND ys.year = :year")
    Optional<YearSummary> findByTrainerUsernameAndYear(@Param("username") String username, @Param("year") int year);
}
