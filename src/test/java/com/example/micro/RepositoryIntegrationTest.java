package com.example.micro;

import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.repository.MonthSummaryRepository;
import com.example.micro.repository.YearSummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the JPA repositories
 * Note: This test only focuses on the relational database repositories
 * and does not test the MongoDB repositories
 */
@DataJpaTest
public class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    // Define a TrainerWorkloadJpaRepository interface or use the correct JPA repository
    // This needs to be created separately from the MongoDB repository
    // @Autowired
    // private TrainerWorkloadJpaRepository trainerWorkloadRepository;

    @Autowired
    private YearSummaryRepository yearSummaryRepository;

    @Autowired
    private MonthSummaryRepository monthSummaryRepository;

    @Test
    void trainerWorkload_PersistAndFind_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());

        // Act
        entityManager.persist(trainer);
        entityManager.flush();

        // Using EntityManager to find the entity since repository is not available
        TrainerWorkload foundTrainer = entityManager.find(TrainerWorkload.class, "john.doe");

        // Assert
        assertNotNull(foundTrainer);
        assertEquals("John", foundTrainer.getFirstName());
        assertEquals("Doe", foundTrainer.getLastName());
        assertTrue(foundTrainer.isActive());
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
        year.setTrainerUsername("john.doe");
        year.setMonths(new ArrayList<>());

        // Act
        entityManager.persist(year);
        entityManager.flush();

        Optional<YearSummary> foundYear = yearSummaryRepository.findById(year.getId());

        // Assert
        assertTrue(foundYear.isPresent());
        assertEquals(2025, foundYear.get().getYear());
        assertEquals("john.doe", foundYear.get().getTrainerUsername());
    }

    @Test
    void yearSummaryRepository_FindByTrainerUsernameAndYear_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("jane.doe");
        trainer.setFirstName("Jane");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainerUsername("jane.doe");
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);
        entityManager.flush();

        // Act
        Optional<YearSummary> foundYear = yearSummaryRepository.findByTrainerUsernameAndYear("jane.doe", 2025);

        // Assert
        assertTrue(foundYear.isPresent());
        assertEquals(2025, foundYear.get().getYear());
        assertEquals("jane.doe", foundYear.get().getTrainerUsername());
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
        year.setTrainerUsername("john.doe");
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);
        entityManager.flush();

        MonthSummary month = new MonthSummary();
        month.setMonth(5);
        month.setSummaryDuration(60);
        month.setYearId(year.getId());

        // Act
        entityManager.persist(month);
        entityManager.flush();

        Optional<MonthSummary> foundMonth = monthSummaryRepository.findById(month.getId());

        // Assert
        assertTrue(foundMonth.isPresent());
        assertEquals(5, foundMonth.get().getMonth());
        assertEquals(60, foundMonth.get().getSummaryDuration());
        assertEquals(year.getId(), foundMonth.get().getYearId());
    }

    @Test
    void monthSummaryRepository_FindByTrainerUsernameAndYearAndMonth_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("jane.doe");
        trainer.setFirstName("Jane");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);

        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainerUsername("jane.doe");
        year.setMonths(new ArrayList<>());
        entityManager.persist(year);

        MonthSummary month = new MonthSummary();
        month.setMonth(6);
        month.setSummaryDuration(90);
        month.setYearId(year.getId());
        entityManager.persist(month);
        entityManager.flush();

        // Act
        Optional<MonthSummary> foundMonth = monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(
                "jane.doe", 2025, 6);

        // Assert
        assertTrue(foundMonth.isPresent());
        assertEquals(6, foundMonth.get().getMonth());
        assertEquals(90, foundMonth.get().getSummaryDuration());
    }

    @Test
    void trainerWorkload_DeleteAndVerify_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        entityManager.persist(trainer);
        entityManager.flush();

        // Act
        entityManager.remove(trainer);
        entityManager.flush();

        TrainerWorkload foundTrainer = entityManager.find(TrainerWorkload.class, "john.doe");

        // Assert
        assertNull(foundTrainer);
    }

    @Test
    void cascadeRelationships_TrainerWithYearsAndMonths_Success() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("cascade.test");
        trainer.setFirstName("Cascade");
        trainer.setLastName("Test");
        trainer.setActive(true);

        List<YearSummary> years = new ArrayList<>();
        YearSummary year = new YearSummary();
        year.setYear(2025);
        year.setTrainerUsername("cascade.test");

        List<MonthSummary> months = new ArrayList<>();
        MonthSummary month = new MonthSummary();
        month.setMonth(7);
        month.setSummaryDuration(120);
        months.add(month);

        year.setMonths(months);
        years.add(year);
        trainer.setYears(years);

        // Save the entire object graph
        entityManager.persist(trainer);
        entityManager.persist(year);

        // Set the year ID on the month after persisting the year
        month.setYearId(year.getId());
        entityManager.persist(month);
        entityManager.flush();

        // Act - retrieve the trainer and verify the relationships
        TrainerWorkload foundTrainer = entityManager.find(TrainerWorkload.class, "cascade.test");

        // Assert
        assertNotNull(foundTrainer);
        assertEquals("Cascade", foundTrainer.getFirstName());

        // Verify year relationship
        Optional<YearSummary> foundYear = yearSummaryRepository.findByTrainerUsernameAndYear("cascade.test", 2025);
        assertTrue(foundYear.isPresent());
        assertEquals(2025, foundYear.get().getYear());

        // Verify month relationship
        Optional<MonthSummary> foundMonth = monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(
                "cascade.test", 2025, 7);
        assertTrue(foundMonth.isPresent());
        assertEquals(7, foundMonth.get().getMonth());
        assertEquals(120, foundMonth.get().getSummaryDuration());
    }
}