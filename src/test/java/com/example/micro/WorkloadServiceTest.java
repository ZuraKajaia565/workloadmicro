package com.example.micro;

import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadUpdateRequest;
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
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.ArrayList;
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

    private static final String USERNAME = "trainer1";
    private static final String TRANSACTION_ID = "test-transaction-id";

    @BeforeEach
    void setUp() {
        MDC.put("transactionId", TRANSACTION_ID);
    }

    @Test
    void updateTrainerWorkload_Add_ExistingTrainer_Success() {
        // Arrange
        WorkloadUpdateRequest request = createWorkloadUpdateRequest(WorkloadUpdateRequest.ActionType.ADD);
        TrainerWorkload existingTrainer = createTrainerWorkload();

        when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenReturn(existingTrainer);

        // Act
        workloadService.updateTrainerWorkload(request);

        // Assert
        verify(trainerWorkloadRepository).findById(USERNAME);
        verify(trainerWorkloadRepository).save(any(TrainerWorkload.class));

        // Verify that the duration was increased
        YearSummary yearSummary = existingTrainer.getYears().get(0);
        MonthSummary monthSummary = yearSummary.getMonths().get(0);
        assertEquals(120, monthSummary.getSummaryDuration()); // 60 (original) + 60 (added)
    }

    @Test
    void updateTrainerWorkload_Add_NewTrainer_Success() {
        // Arrange
        WorkloadUpdateRequest request = createWorkloadUpdateRequest(WorkloadUpdateRequest.ActionType.ADD);

        when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.empty());
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        workloadService.updateTrainerWorkload(request);

        // Assert
        verify(trainerWorkloadRepository).findById(USERNAME);
        verify(trainerWorkloadRepository).save(any(TrainerWorkload.class));
    }

    @Test
    void updateTrainerWorkload_Delete_Success() {
        // Arrange
        WorkloadUpdateRequest request = createWorkloadUpdateRequest(WorkloadUpdateRequest.ActionType.DELETE);
        TrainerWorkload existingTrainer = createTrainerWorkload();

        when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenReturn(existingTrainer);

        // Act
        workloadService.updateTrainerWorkload(request);

        // Assert
        verify(trainerWorkloadRepository).findById(USERNAME);
        verify(trainerWorkloadRepository).save(any(TrainerWorkload.class));

        // Verify that the duration was decreased
        YearSummary yearSummary = existingTrainer.getYears().get(0);
        MonthSummary monthSummary = yearSummary.getMonths().get(0);
        assertEquals(0, monthSummary.getSummaryDuration()); // 60 (original) - 60 (deleted) = 0
    }

    @Test
    void updateTrainerWorkload_Delete_BecomesZero_Success() {
        // Arrange
        WorkloadUpdateRequest request = createWorkloadUpdateRequest(WorkloadUpdateRequest.ActionType.DELETE);
        request.setTrainingDuration(100); // More than the current duration
        TrainerWorkload existingTrainer = createTrainerWorkload();

        when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(existingTrainer));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class))).thenReturn(existingTrainer);

        // Act
        workloadService.updateTrainerWorkload(request);

        // Assert
        verify(trainerWorkloadRepository).findById(USERNAME);
        verify(trainerWorkloadRepository).save(any(TrainerWorkload.class));

        // Verify that the duration was set to 0 (not negative)
        YearSummary yearSummary = existingTrainer.getYears().get(0);
        MonthSummary monthSummary = yearSummary.getMonths().get(0);
        assertEquals(0, monthSummary.getSummaryDuration());
    }

    @Test
    void getMonthlyWorkload_Found_Success() {
        // Arrange
        int year = 2025;
        int month = 5;

        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setMonth(month);
        monthSummary.setSummaryDuration(60);

        YearSummary yearSummary = new YearSummary();
        yearSummary.setYear(year);

        TrainerWorkload trainerWorkload = new TrainerWorkload();
        trainerWorkload.setUsername(USERNAME);
        trainerWorkload.setFirstName("John");
        trainerWorkload.setLastName("Doe");
        trainerWorkload.setActive(true);

        monthSummary.setYearSummary(yearSummary);
        yearSummary.setTrainer(trainerWorkload);

        when(monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(USERNAME, year, month))
                .thenReturn(Optional.of(monthSummary));

        // Act
        MonthlyWorkloadResponse response = workloadService.getMonthlyWorkload(USERNAME, year, month);

        // Assert
        verify(monthSummaryRepository).findByTrainerUsernameAndYearAndMonth(USERNAME, year, month);
        assertEquals(USERNAME, response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(true, response.isActive());
        assertEquals(year, response.getYear());
        assertEquals(month, response.getMonth());
        assertEquals(60, response.getSummaryDuration());
    }

    @Test
    void getMonthlyWorkload_NotFound_ReturnsEmptyResponse() {
        // Arrange
        int year = 2025;
        int month = 5;

        when(monthSummaryRepository.findByTrainerUsernameAndYearAndMonth(USERNAME, year, month))
                .thenReturn(Optional.empty());

        // Act
        MonthlyWorkloadResponse response = workloadService.getMonthlyWorkload(USERNAME, year, month);

        // Assert
        verify(monthSummaryRepository).findByTrainerUsernameAndYearAndMonth(USERNAME, year, month);
        assertEquals(USERNAME, response.getUsername());
        assertEquals("", response.getFirstName());
        assertEquals("", response.getLastName());
        assertEquals(false, response.isActive());
        assertEquals(year, response.getYear());
        assertEquals(month, response.getMonth());
        assertEquals(0, response.getSummaryDuration());
    }

    @Test
    void getTrainerWorkloadSummary_Found_Success() {
        // Arrange
        TrainerWorkload trainerWorkload = createTrainerWorkload();

        when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainerWorkload));

        // Act
        TrainerWorkloadResponse response = workloadService.getTrainerWorkloadSummary(USERNAME);

        // Assert
        verify(trainerWorkloadRepository).findById(USERNAME);
        assertEquals(USERNAME, response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(true, response.isActive());
        assertEquals(1, response.getYears().size());
    }

    @Test
    void getTrainerWorkloadSummary_NotFound_ReturnsEmptyResponse() {
        // Arrange
        when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.empty());

        // Act
        TrainerWorkloadResponse response = workloadService.getTrainerWorkloadSummary(USERNAME);

        // Assert
        verify(trainerWorkloadRepository).findById(USERNAME);
        assertEquals(USERNAME, response.getUsername());
        assertEquals("", response.getFirstName());
        assertEquals("", response.getLastName());
        assertEquals(false, response.isActive());
        assertEquals(0, response.getYears().size());
    }

    // Helper methods
    private WorkloadUpdateRequest createWorkloadUpdateRequest(WorkloadUpdateRequest.ActionType actionType) {
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2025, 5, 8));
        request.setTrainingDuration(60);
        request.setActionType(actionType);
        return request;
    }

    private TrainerWorkload createTrainerWorkload() {
        TrainerWorkload trainerWorkload = new TrainerWorkload();
        trainerWorkload.setUsername(USERNAME);
        trainerWorkload.setFirstName("John");
        trainerWorkload.setLastName("Doe");
        trainerWorkload.setActive(true);

        YearSummary yearSummary = new YearSummary();
        yearSummary.setId(1L);
        yearSummary.setYear(2025);
        yearSummary.setTrainer(trainerWorkload);

        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setId(1L);
        monthSummary.setMonth(5);
        monthSummary.setSummaryDuration(60);
        monthSummary.setYearSummary(yearSummary);

        yearSummary.setMonths(new ArrayList<>());
        yearSummary.getMonths().add(monthSummary);

        trainerWorkload.setYears(new ArrayList<>());
        trainerWorkload.getYears().add(yearSummary);

        return trainerWorkload;
    }
}