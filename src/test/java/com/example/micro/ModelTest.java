package com.example.micro;

import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
/*
    @Test
    void trainerWorkload_GetOrCreateYear_ExistingYear_ReturnsThatYear() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        YearSummary existingYear = new YearSummary();
        existingYear.setYear(2025);
        existingYear.setTrainer(trainer);

        List<YearSummary> years = new ArrayList<>();
        years.add(existingYear);
        trainer.setYears(years);

        // Act
        YearSummary result = trainer.getOrCreateYear(2025);

        // Assert
        assertSame(existingYear, result, "Should return the existing year instance");
        assertEquals(1, trainer.getYears().size(), "Should not add a new year");
    }

    @Test
    void trainerWorkload_GetOrCreateYear_NewYear_CreatesAndReturnsIt() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setYears(new ArrayList<>());

        // Act
        YearSummary result = trainer.getOrCreateYear(2025);

        // Assert
        assertNotNull(result, "Should create and return a new year");
        assertEquals(2025, result.getYear(), "New year should have the correct year value");
        assertSame(trainer, result.getTrainer(), "New year should reference back to the trainer");
        assertEquals(1, trainer.getYears().size(), "Should add the new year to the trainer's years list");
        assertSame(result, trainer.getYears().get(0), "New year should be in the trainer's years list");
    }

    @Test
    void yearSummary_GetOrCreateMonth_ExistingMonth_ReturnsThatMonth() {
        // Arrange
        YearSummary year = new YearSummary();
        MonthSummary existingMonth = new MonthSummary();
        existingMonth.setMonth(5);
        existingMonth.setYearSummary(year);

        List<MonthSummary> months = new ArrayList<>();
        months.add(existingMonth);
        year.setMonths(months);

        // Act
        MonthSummary result = year.getOrCreateMonth(5);

        // Assert
        assertSame(existingMonth, result, "Should return the existing month instance");
        assertEquals(1, year.getMonths().size(), "Should not add a new month");
    }

    @Test
    void yearSummary_GetOrCreateMonth_NewMonth_CreatesAndReturnsIt() {
        // Arrange
        YearSummary year = new YearSummary();
        year.setMonths(new ArrayList<>());

        // Act
        MonthSummary result = year.getOrCreateMonth(5);

        // Assert
        assertNotNull(result, "Should create and return a new month");
        assertEquals(5, result.getMonth(), "New month should have the correct month value");
        assertEquals(0, result.getSummaryDuration(), "New month should have zero duration initially");
        assertSame(year, result.getYearSummary(), "New month should reference back to the year");
        assertEquals(1, year.getMonths().size(), "Should add the new month to the year's months list");
        assertSame(result, year.getMonths().get(0), "New month should be in the year's months list");
    }

    @Test
    void trainerWorkload_Setters_GettersWork() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();

        // Act
        trainer.setUsername("john.doe");
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setActive(true);

        List<YearSummary> years = new ArrayList<>();
        trainer.setYears(years);

        // Assert
        assertEquals("john.doe", trainer.getUsername());
        assertEquals("John", trainer.getFirstName());
        assertEquals("Doe", trainer.getLastName());
        assertTrue(trainer.isActive());
        assertSame(years, trainer.getYears());
    }

    @Test
    void yearSummary_Setters_GettersWork() {
        // Arrange
        YearSummary year = new YearSummary();

        // Act
        year.setId(1L);
        year.setYear(2025);

        TrainerWorkload trainer = new TrainerWorkload();
        year.setTrainer(trainer);

        List<MonthSummary> months = new ArrayList<>();
        year.setMonths(months);

        // Assert
        assertEquals(1L, year.getId());
        assertEquals(2025, year.getYear());
        assertSame(trainer, year.getTrainer());
        assertSame(months, year.getMonths());
    }

    @Test
    void monthSummary_Setters_GettersWork() {
        // Arrange
        MonthSummary month = new MonthSummary();

        // Act
        month.setId(1L);
        month.setMonth(5);
        month.setSummaryDuration(60);

        YearSummary year = new YearSummary();
        month.setYearSummary(year);

        // Assert
        assertEquals(1L, month.getId());
        assertEquals(5, month.getMonth());
        assertEquals(60, month.getSummaryDuration());
        assertSame(year, month.getYearSummary());
    }

    @Test
    void trainerWorkload_AllArgsConstructor_Works() {
        // Arrange
        String username = "john.doe";
        String firstName = "John";
        String lastName = "Doe";
        boolean isActive = true;
        List<YearSummary> years = new ArrayList<>();

        // Act
        TrainerWorkload trainer = new TrainerWorkload(username, firstName, lastName, isActive, years);

        // Assert
        assertEquals(username, trainer.getUsername());
        assertEquals(firstName, trainer.getFirstName());
        assertEquals(lastName, trainer.getLastName());
        assertEquals(isActive, trainer.isActive());
        assertSame(years, trainer.getYears());
    }

    @Test
    void yearSummary_AllArgsConstructor_Works() {
        // Arrange
        Long id = 1L;
        int year = 2025;
        TrainerWorkload trainer = new TrainerWorkload();
        List<MonthSummary> months = new ArrayList<>();

        // Act
        YearSummary yearSummary = new YearSummary(id, year, trainer, months);

        // Assert
        assertEquals(id, yearSummary.getId());
        assertEquals(year, yearSummary.getYear());
        assertSame(trainer, yearSummary.getTrainer());
        assertSame(months, yearSummary.getMonths());
    }

    @Test
    void monthSummary_AllArgsConstructor_Works() {
        // Arrange
        Long id = 1L;
        int month = 5;
        int summaryDuration = 60;
        YearSummary year = new YearSummary();

        // Act
        MonthSummary monthSummary = new MonthSummary(id, month, summaryDuration, year);

        // Assert
        assertEquals(id, monthSummary.getId());
        assertEquals(month, monthSummary.getMonth());
        assertEquals(summaryDuration, monthSummary.getSummaryDuration());
        assertSame(year, monthSummary.getYearSummary());
    }
*/
}
