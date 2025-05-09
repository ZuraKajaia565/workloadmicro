package com.example.micro;

import com.example.micro.controller.WorkloadController;
import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadRequest;
import com.example.micro.exception.InsufficientWorkloadException;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.model.MonthSummary;
import com.example.micro.model.TrainerWorkload;
import com.example.micro.model.YearSummary;
import com.example.micro.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadController workloadController;

    private final String username = "trainer1";
    private final int year = 2025;
    private final int month = 5;
    private final int duration = 60;

    private TrainerWorkload trainerWorkload;
    private YearSummary yearSummary;
    private MonthSummary monthSummary;
    private WorkloadRequest workloadRequest;
    private TrainerWorkloadResponse trainerWorkloadResponse;

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

        workloadRequest = new WorkloadRequest();
        workloadRequest.setFirstName("John");
        workloadRequest.setLastName("Doe");
        workloadRequest.setActive(true);
        workloadRequest.setTrainingDuration(duration);

        // Create a valid TrainerWorkloadResponse for the getWorkloadSummary tests
        trainerWorkloadResponse = new TrainerWorkloadResponse();
        trainerWorkloadResponse.setUsername(username);
        trainerWorkloadResponse.setFirstName("John");
        trainerWorkloadResponse.setLastName("Doe");
        trainerWorkloadResponse.setActive(true);

        // Add year data
        TrainerWorkloadResponse.YearSummaryDto yearDto = new TrainerWorkloadResponse.YearSummaryDto();
        yearDto.setYear(year);

        // Add month data
        TrainerWorkloadResponse.MonthSummaryDto monthDto = new TrainerWorkloadResponse.MonthSummaryDto();
        monthDto.setMonth(month);
        monthDto.setSummaryDuration(duration);

        // Build the structure
        List<TrainerWorkloadResponse.MonthSummaryDto> monthDtos = new ArrayList<>();
        monthDtos.add(monthDto);
        yearDto.setMonths(monthDtos);

        List<TrainerWorkloadResponse.YearSummaryDto> yearDtos = new ArrayList<>();
        yearDtos.add(yearDto);
        trainerWorkloadResponse.setYears(yearDtos);
    }

    @Test
    void updateWorkload_Success_ReturnsOk() {
        // Arrange
        when(workloadService.updateOrCreateWorkload(
                eq(username), eq(year), eq(month),
                eq(workloadRequest.getFirstName()),
                eq(workloadRequest.getLastName()),
                eq(workloadRequest.isActive()),
                eq(workloadRequest.getTrainingDuration())))
                .thenReturn(trainerWorkload);

        // Act
        ResponseEntity<Void> response = workloadController.updateWorkload(
                username, year, month, workloadRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workloadService).updateOrCreateWorkload(
                username, year, month,
                workloadRequest.getFirstName(),
                workloadRequest.getLastName(),
                workloadRequest.isActive(),
                workloadRequest.getTrainingDuration());
    }

    @Test
    void updateWorkload_Exception_ReturnsInternalServerError() {
        // Arrange
        when(workloadService.updateOrCreateWorkload(
                anyString(), anyInt(), anyInt(),
                anyString(), anyString(), anyBoolean(), anyInt()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        ResponseEntity<Void> response = workloadController.updateWorkload(
                username, year, month, workloadRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(workloadService).updateOrCreateWorkload(
                username, year, month,
                workloadRequest.getFirstName(),
                workloadRequest.getLastName(),
                workloadRequest.isActive(),
                workloadRequest.getTrainingDuration());
    }

    @Test
    void getMonthlyWorkload_Success_ReturnsMonthlyWorkload() {
        // Arrange
        when(workloadService.getMonthlyWorkload(username, year, month)).thenReturn(monthSummary);
        when(workloadService.getTrainerById(username)).thenReturn(trainerWorkload);

        // Act
        ResponseEntity<MonthlyWorkloadResponse> response = workloadController.getMonthlyWorkload(
                username, year, month);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());
        assertEquals(year, response.getBody().getYear());
        assertEquals(month, response.getBody().getMonth());
        assertEquals(duration, response.getBody().getSummaryDuration());

        verify(workloadService).getMonthlyWorkload(username, year, month);
        verify(workloadService).getTrainerById(username);
    }

    @Test
    void getMonthlyWorkload_NotFound_ReturnsNotFound() {
        // Arrange
        when(workloadService.getMonthlyWorkload(username, year, month))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act
        ResponseEntity<MonthlyWorkloadResponse> response = workloadController.getMonthlyWorkload(
                username, year, month);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workloadService).getMonthlyWorkload(username, year, month);
        verify(workloadService, never()).getTrainerById(anyString());
    }

    @Test
    void deleteWorkload_Success_ReturnsNoContent() {
        // Arrange
        doNothing().when(workloadService).deleteWorkload(username, year, month);

        // Act
        ResponseEntity<Void> response = workloadController.deleteWorkload(username, year, month);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(workloadService).deleteWorkload(username, year, month);
    }

    @Test
    void deleteWorkload_NotFound_ReturnsNotFound() {
        // Arrange
        doThrow(new ResourceNotFoundException("Not found"))
                .when(workloadService).deleteWorkload(username, year, month);

        // Act
        ResponseEntity<Void> response = workloadController.deleteWorkload(username, year, month);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workloadService).deleteWorkload(username, year, month);
    }

    @Test
    void addWorkload_Success_ReturnsOk() {
        // Arrange
        when(workloadService.addWorkload(username, year, month, duration))
                .thenReturn(trainerWorkload);

        // Act
        ResponseEntity<Void> response = workloadController.addWorkload(
                username, year, month, duration);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workloadService).addWorkload(username, year, month, duration);
    }

    @Test
    void addWorkload_NotFound_ReturnsNotFound() {
        // Arrange
        when(workloadService.addWorkload(username, year, month, duration))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act
        ResponseEntity<Void> response = workloadController.addWorkload(
                username, year, month, duration);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workloadService).addWorkload(username, year, month, duration);
    }

    @Test
    void subtractWorkload_Success_ReturnsOk() {
        // Arrange
        when(workloadService.subtractWorkload(username, year, month, duration))
                .thenReturn(trainerWorkload);

        // Act
        ResponseEntity<Void> response = workloadController.subtractWorkload(
                username, year, month, duration);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workloadService).subtractWorkload(username, year, month, duration);
    }

    @Test
    void subtractWorkload_NotFound_ReturnsNotFound() {
        // Arrange
        when(workloadService.subtractWorkload(username, year, month, duration))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act
        ResponseEntity<Void> response = workloadController.subtractWorkload(
                username, year, month, duration);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workloadService).subtractWorkload(username, year, month, duration);
    }

    @Test
    void subtractWorkload_InsufficientWorkload_ReturnsBadRequest() {
        // Arrange
        when(workloadService.subtractWorkload(username, year, month, duration))
                .thenThrow(new InsufficientWorkloadException("Insufficient workload"));

        // Act
        ResponseEntity<Void> response = workloadController.subtractWorkload(
                username, year, month, duration);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(workloadService).subtractWorkload(username, year, month, duration);
    }

    @Test
    void getWorkloadSummary_Success_ReturnsSummary() {
        // Arrange
        when(workloadService.getTrainerWorkloadSummary(username)).thenReturn(trainerWorkload);

        // Act
        // Let's use getWorkloadSummary as the method name, which is likely the actual name in your controller
        ResponseEntity<TrainerWorkloadResponse> response = workloadController.getWorkloadSummary(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());

        verify(workloadService).getTrainerWorkloadSummary(username);
    }

    @Test
    void getWorkloadSummary_NotFound_ReturnsNotFound() {
        // Arrange
        when(workloadService.getTrainerWorkloadSummary(username))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act
        // Using getWorkloadSummary instead of getTrainerWorkloadSummary
        ResponseEntity<TrainerWorkloadResponse> response = workloadController.getWorkloadSummary(username);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workloadService).getTrainerWorkloadSummary(username);
    }

    // Helper method to mock the controller's conversion logic if needed
    private TrainerWorkloadResponse convertToSummaryResponse(TrainerWorkload trainerWorkload) {
        return trainerWorkloadResponse; // Return the prepared response for testing
    }
}