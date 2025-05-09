package com.example.micro;

import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadRequest;
import com.example.micro.exception.InsufficientWorkloadException;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.repository.MonthSummaryRepository;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.repository.YearSummaryRepository;
import com.example.micro.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private YearSummaryRepository yearSummaryRepository;

    @Mock
    private MonthSummaryRepository monthSummaryRepository;

    @InjectMocks
    private WorkloadService workloadService;

    private TrainerWorkload trainerWorkload;
    private YearSummary yearSummary;
    private MonthSummary monthSummary;
    private final String username = "trainer1";
    private final int year = 2025;
    private final int month = 5;
    private final int duration = 60;

    @BeforeEach
    void setUp() {
        // Initialize test data
        trainerWorkload = new TrainerWorkload();
        trainerWorkload.setUsername(username);
        trainerWorkload.setFirstName("John");
        trainerWorkload.setLastName("Doe");
        trainerWorkload.setActive(true);
        trainerWorkload.setYears(new ArrayList<>());

        yearSummary = new YearSummary();
        yearSummary.setId(1L);
        yearSummary.setYear(year);
        yearSummary.setTrainerUsername(username);
        yearSummary.setMonths(new ArrayList<>());

        monthSummary = new MonthSummary();
        monthSummary.setId(1L);
        monthSummary.setMonth(month);
        monthSummary.setSummaryDuration(duration);
        monthSummary.setYearId(yearSummary.getId());
        monthSummary.setYearSummary(yearSummary);

        yearSummary.getMonths().add(monthSummary);
        trainerWorkload.getYears().add(yearSummary);
    }

    @Test
    void getTrainerById_ExistingTrainer_ReturnsTrainer() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.of(trainerWorkload));

        // Act
        TrainerWorkload result = workloadService.getTrainerById(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(trainerWorkloadRepository).findById(username);
    }

    @Test
    void getTrainerById_NonExistingTrainer_ThrowsException() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workloadService.getTrainerById(username));
        verify(trainerWorkloadRepository).findById(username);
    }

    @Test
    void updateOrCreateWorkload_NewTrainer_CreatesTrainerAndWorkload() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.empty());
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        TrainerWorkload result = workloadService.updateOrCreateWorkload(
                username, year, month, "John", "Doe", true, duration);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.isActive());
        assertEquals(1, result.getYears().size());

        YearSummary resultYear = result.getYears().get(0);
        assertEquals(year, resultYear.getYear());
        assertEquals(1, resultYear.getMonths().size());

        MonthSummary resultMonth = resultYear.getMonths().get(0);
        assertEquals(month, resultMonth.getMonth());
        assertEquals(duration, resultMonth.getSummaryDuration());

        verify(trainerWorkloadRepository).findById(username);
        verify(trainerWorkloadRepository).save(any(TrainerWorkload.class));
    }

    @Test
    void updateOrCreateWorkload_ExistingTrainer_UpdatesWorkload() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.of(trainerWorkload));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        int newDuration = 120;

        // Act
        TrainerWorkload result = workloadService.updateOrCreateWorkload(
                username, year, month, "John", "Doe", true, newDuration);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getYears().size());
        assertEquals(1, result.getYears().get(0).getMonths().size());
        assertEquals(newDuration, result.getYears().get(0).getMonths().get(0).getSummaryDuration());

        verify(trainerWorkloadRepository).findById(username);
        verify(trainerWorkloadRepository).save(any(TrainerWorkload.class));
    }

    @Test
    void getMonthlyWorkload_ExistingWorkload_ReturnsMonthSummary() {
        // Arrange
        when(monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(username, year, month))
                .thenReturn(Optional.of(monthSummary));

        // Act
        MonthSummary result = workloadService.getMonthlyWorkload(username, year, month);

        // Assert
        assertNotNull(result);
        assertEquals(month, result.getMonth());
        assertEquals(duration, result.getSummaryDuration());

        verify(monthSummaryRepository).findByTrainerUsernameAndYearAndMonth(username, year, month);
    }

    @Test
    void getMonthlyWorkload_NonExistingWorkload_ThrowsException() {
        // Arrange
        when(monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(username, year, month))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workloadService.getMonthlyWorkload(username, year, month));

        verify(monthSummaryRepository).findByTrainerUsernameAndYearAndMonth(username, year, month);
    }



    @Test
    void deleteWorkload_NonExistingTrainer_ThrowsException() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workloadService.deleteWorkload(username, year, month));

        verify(trainerWorkloadRepository).findById(username);
        verify(trainerWorkloadRepository, never()).save(any(TrainerWorkload.class));
    }

    @Test
    void addWorkload_ExistingWorkload_AddsToWorkload() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.of(trainerWorkload));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenReturn(trainerWorkload);

        int additionalDuration = 30;
        int expectedDuration = duration + additionalDuration;

        // Act
        TrainerWorkload result = workloadService.addWorkload(username, year, month, additionalDuration);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDuration, result.getYears().get(0).getMonths().get(0).getSummaryDuration());

        verify(trainerWorkloadRepository).findById(username);
        verify(trainerWorkloadRepository).save(trainerWorkload);
    }

    @Test
    void subtractWorkload_SufficientWorkload_SubtractsFromWorkload() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.of(trainerWorkload));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenReturn(trainerWorkload);

        int subtractDuration = 30;
        int expectedDuration = duration - subtractDuration;

        // Act
        TrainerWorkload result = workloadService.subtractWorkload(username, year, month, subtractDuration);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDuration, result.getYears().get(0).getMonths().get(0).getSummaryDuration());

        verify(trainerWorkloadRepository).findById(username);
        verify(trainerWorkloadRepository).save(trainerWorkload);
    }

    @Test
    void subtractWorkload_InsufficientWorkload_ThrowsException() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.of(trainerWorkload));

        int subtractDuration = duration + 10; // More than available

        // Act & Assert
        assertThrows(InsufficientWorkloadException.class,
                () -> workloadService.subtractWorkload(username, year, month, subtractDuration));

        verify(trainerWorkloadRepository).findById(username);
        verify(trainerWorkloadRepository, never()).save(any(TrainerWorkload.class));
    }

    @Test
    void getTrainerWorkloadSummary_ExistingTrainer_ReturnsTrainerWorkload() {
        // Arrange
        when(trainerWorkloadRepository.findById(username)).thenReturn(Optional.of(trainerWorkload));

        // Act
        TrainerWorkload result = workloadService.getTrainerWorkloadSummary(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(1, result.getYears().size());

        verify(trainerWorkloadRepository).findById(username);
    }
}