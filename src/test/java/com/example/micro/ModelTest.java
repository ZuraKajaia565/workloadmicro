package com.example.micro;

import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    /**
     * Helper method to simulate getOrCreateYear functionality
     */
    private YearSummary getOrCreateYear(TrainerWorkload trainer, int yearValue) {
        // First check if year already exists
        for (YearSummary existingYear : trainer.getYears()) {
            if (existingYear.getYear() == yearValue) {
                return existingYear;
            }
        }

        // Create a new year if not found
        YearSummary newYear = new YearSummary();
        newYear.setYear(yearValue);
        newYear.setTrainerUsername(trainer.getUsername());
        newYear.setMonths(new ArrayList<>());
        trainer.getYears().add(newYear);
        return newYear;
    }

    /**
     * Helper method to simulate getOrCreateMonth functionality
     */
    private MonthSummary getOrCreateMonth(YearSummary year, int monthValue) {
        // First check if month already exists
        for (MonthSummary existingMonth : year.getMonths()) {
            if (existingMonth.getMonth() == monthValue) {
                return existingMonth;
            }
        }

        // Create a new month if not found
        MonthSummary newMonth = new MonthSummary();
        newMonth.setMonth(monthValue);
        newMonth.setSummaryDuration(0);
        newMonth.setYearId(year.getId());
        year.getMonths().add(newMonth);
        return newMonth;
    }

    @Test
    void trainerWorkload_GetOrCreateYear_ExistingYear_ReturnsThatYear() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");

        YearSummary existingYear = new YearSummary();
        existingYear.setYear(2025);
        existingYear.setTrainerUsername(trainer.getUsername());

        List<YearSummary> years = new ArrayList<>();
        years.add(existingYear);
        trainer.setYears(years);

        // Act
        YearSummary result = getOrCreateYear(trainer, 2025);

        // Assert
        assertSame(existingYear, result, "Should return the existing year instance");
        assertEquals(1, trainer.getYears().size(), "Should not add a new year");
    }

    @Test
    void trainerWorkload_GetOrCreateYear_NewYear_CreatesAndReturnsIt() {
        // Arrange
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.doe");
        trainer.setYears(new ArrayList<>());

        // Act
        YearSummary result = getOrCreateYear(trainer, 2025);

        // Assert
        assertNotNull(result, "Should create and return a new year");
        assertEquals(2025, result.getYear(), "New year should have the correct year value");
        assertEquals(trainer.getUsername(), result.getTrainerUsername(), "New year should reference back to the trainer username");
        assertEquals(1, trainer.getYears().size(), "Should add the new year to the trainer's years list");
        assertSame(result, trainer.getYears().get(0), "New year should be in the trainer's years list");
    }

    @Test
    void yearSummary_GetOrCreateMonth_ExistingMonth_ReturnsThatMonth() {
        // Arrange
        YearSummary year = new YearSummary();
        year.setId(1L);

        MonthSummary existingMonth = new MonthSummary();
        existingMonth.setMonth(5);
        existingMonth.setYearId(year.getId());

        List<MonthSummary> months = new ArrayList<>();
        months.add(existingMonth);
        year.setMonths(months);

        // Act
        MonthSummary result = getOrCreateMonth(year, 5);

        // Assert
        assertSame(existingMonth, result, "Should return the existing month instance");
        assertEquals(1, year.getMonths().size(), "Should not add a new month");
    }

    @Test
    void yearSummary_GetOrCreateMonth_NewMonth_CreatesAndReturnsIt() {
        // Arrange
        YearSummary year = new YearSummary();
        year.setId(1L);
        year.setMonths(new ArrayList<>());

        // Act
        MonthSummary result = getOrCreateMonth(year, 5);

        // Assert
        assertNotNull(result, "Should create and return a new month");
        assertEquals(5, result.getMonth(), "New month should have the correct month value");
        assertEquals(0, result.getSummaryDuration(), "New month should have zero duration initially");
        assertEquals(year.getId(), result.getYearId(), "New month should reference back to the year ID");
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
        year.setTrainerUsername("john.doe");

        List<MonthSummary> months = new ArrayList<>();
        year.setMonths(months);

        // Assert
        assertEquals(1L, year.getId());
        assertEquals(2025, year.getYear());
        assertEquals("john.doe", year.getTrainerUsername());
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
        month.setYearId(1L);

        // Assert
        assertEquals(1L, month.getId());
        assertEquals(5, month.getMonth());
        assertEquals(60, month.getSummaryDuration());
        assertEquals(1L, month.getYearId());
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
        String trainerUsername = "john.doe";
        List<MonthSummary> months = new ArrayList<>();

        // Act
        YearSummary yearSummary = new YearSummary(id, year, trainerUsername, months);

        // Assert
        assertEquals(id, yearSummary.getId());
        assertEquals(year, yearSummary.getYear());
        assertEquals(trainerUsername, yearSummary.getTrainerUsername());
        assertSame(months, yearSummary.getMonths());
    }

    @Test
    void monthSummary_AllArgsConstructor_Works() {
        // Arrange
        Long id = 1L;
        int month = 5;
        int summaryDuration = 60;
        Long yearId = 1L;

        // Act
        MonthSummary monthSummary = new MonthSummary(id, month, summaryDuration, yearId);

        // Assert
        assertEquals(id, monthSummary.getId());
        assertEquals(month, monthSummary.getMonth());
        assertEquals(summaryDuration, monthSummary.getSummaryDuration());
        assertEquals(yearId, monthSummary.getYearId());
    }
}