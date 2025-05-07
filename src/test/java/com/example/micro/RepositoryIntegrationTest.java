package com.example.micro;

import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.repository.MonthSummaryRepository;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.repository.YearSummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private YearSummaryRepository yearSummaryRepository;

    @Autowired
    private MonthSummaryRepository monthSummaryRepository;

    @Test
    void trainerWorkloadRepository_Save_FindById_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());

        // Act
        TrainerWorkload savedTrainer = trainerWorkloadRepository.save(trainer);
        Optional<TrainerWorkload> foundTrainer = trainerWorkloadRepository.findById("john.doe");

        // Assert
        assertTrue(foundTrainer.isPresent());
        assertEquals("John", foundTrainer.get().getFirstName());
        assertEquals("Doe", foundTrainer.get().getLastName());
        assertTrue(foundTrainer.get().isActive());
    }

    @Test
    void yearSummaryRepository_Save_FindById_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainer(trainer);
        year.setMonths(new ArrayList<>());

        // Act
        YearSummary savedYear = yearSummaryRepository.save(year);
        Optional<YearSummary> foundYear = yearSummaryRepository.findById(savedYear.getId());

        // Assert
        assertTrue(foundYear.isPresent());
        assertEquals(2025, foundYear.get().getYear());
        assertEquals("john.doe", foundYear.get().getTrainer().getUsername());
    }

    @Test
    void monthSummaryRepository_Save_FindById_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainer(trainer);
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);

        MonthSummary month = new MonthSummary();
        month.setMonth(5);
        month.setSummaryDuration(60);
        month.setYearSummary(year);

        // Act
        MonthSummary savedMonth = monthSummaryRepository.save(month);
        Optional<MonthSummary> foundMonth = monthSummaryRepository.findById(savedMonth.getId());

        // Assert
        assertTrue(foundMonth.isPresent());
        assertEquals(5, foundMonth.get().getMonth());
        assertEquals(60, foundMonth.get().getSummaryDuration());
        assertEquals(2025, foundMonth.get().getYearSummary().getYear());
    }

    @Test
    void yearSummaryRepository_FindByTrainerUsernameAndYear_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainer(trainer);
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);

        // Act
        Optional<YearSummary> foundYear = yearSummaryRepository.findByTrainerUsernameAndYear("john.doe", 2025);

        // Assert
        assertTrue(foundYear.isPresent());
        assertEquals(2025, foundYear.get().getYear());
        assertEquals("john.doe", foundYear.get().getTrainer().getUsername());
    }

    @Test
    void monthSummaryRepository_FindByTrainerUsernameAndYearAndMonth_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainer(trainer);
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);

        MonthSummary month = new MonthSummary();
        month.setMonth(5);
        month.setSummaryDuration(60);
        month.setYearSummary(year);
        entityManager.persist(month);

        // Act
        Optional<MonthSummary> foundMonth = monthSummaryRepository.findByTrainerUsernameAndYearAndMonth("john.doe", 2025, 5);

        // Assert
        assertTrue(foundMonth.isPresent());
        assertEquals(5, foundMonth.get().getMonth());
        assertEquals(60, foundMonth.get().getSummaryDuration());
        assertEquals(2025, foundMonth.get().getYearSummary().getYear());
        assertEquals("john.doe", foundMonth.get().getYearSummary().getTrainer().getUsername());
    }

    @Test
    void trainerWorkloadRepository_Delete_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        // Act
        trainerWorkloadRepository.deleteById("john.doe");
        Optional<TrainerWorkload> foundTrainer = trainerWorkloadRepository.findById("john.doe");

        // Assert
        assertFalse(foundTrainer.isPresent());
    }

    @Test
    void cascadeDelete_DeleteTrainerWorkload_DeletesYearsAndMonths() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainer(trainer);
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);

        MonthSummary month = new MonthSummary();
        month.setMonth(5);
        month.setSummaryDuration(60);
        month.setYearSummary(year);
        entityManager.persist(month);

        Long yearId = year.getId();
        Long monthId = month.getId();

        // Act
        trainerWorkloadRepository.deleteById("john.doe");

        assertFalse(trainerWorkloadRepository.findById("john.doe").isPresent());
    }
}