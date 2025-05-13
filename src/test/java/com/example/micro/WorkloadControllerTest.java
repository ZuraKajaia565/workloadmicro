package com.example.micro;

import com.example.micro.controller.WorkloadController;
import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadController controller;

    private TrainerWorkloadDocument trainerDocument;

    @BeforeEach
    void setUp() {
        // Create a sample trainer document
        trainerDocument = new TrainerWorkloadDocument();
        trainerDocument.setUsername("trainer1");
        trainerDocument.setFirstName("John");
        trainerDocument.setLastName("Doe");
        trainerDocument.setActive(true);

        // Add year and month
        TrainerWorkloadDocument.YearSummary yearSummary = new TrainerWorkloadDocument.YearSummary();
        yearSummary.setYear(2025);

        TrainerWorkloadDocument.MonthSummary monthSummary = new TrainerWorkloadDocument.MonthSummary();
        monthSummary.setMonth(5);
        monthSummary.setTrainingsSummaryDuration(60);

        yearSummary.getMonths().add(monthSummary);
        trainerDocument.getYears().add(yearSummary);
    }

    @Test
    void getTrainerWorkload_Found_Success() {
        // Arrange
        when(workloadService.getTrainerWorkload("trainer1")).thenReturn(trainerDocument);

        // Act
        ResponseEntity<?> response = controller.getTrainerWorkload("trainer1");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TrainerWorkloadDocument);
        TrainerWorkloadDocument result = (TrainerWorkloadDocument) response.getBody();
        assertEquals("trainer1", result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(1, result.getYears().size());
        assertEquals(1, result.getYears().get(0).getMonths().size());
        assertEquals(60, result.getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());

        verify(workloadService).getTrainerWorkload("trainer1");
    }

    @Test
    void getTrainerWorkload_NotFound() {
        // Arrange
        when(workloadService.getTrainerWorkload("nonexistent")).thenThrow(new ResourceNotFoundException("Trainer not found: nonexistent"));

        // Act
        ResponseEntity<?> response = controller.getTrainerWorkload("nonexistent");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(workloadService).getTrainerWorkload("nonexistent");
    }

    @Test
    void searchTrainersByName_Found_Success() {
        // Arrange
        List<TrainerWorkloadDocument> trainers = new ArrayList<>();
        trainers.add(trainerDocument);

        when(workloadService.findTrainersByFullName("John", "Doe")).thenReturn(trainers);

        // Act
        ResponseEntity<?> response = controller.searchTrainersByName("John", "Doe");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<TrainerWorkloadDocument> result = (List<TrainerWorkloadDocument>) response.getBody();

        assertEquals(1, result.size());
        assertEquals("trainer1", result.get(0).getUsername());

        verify(workloadService).findTrainersByFullName("John", "Doe");
    }

    @Test
    void searchTrainersByName_NotFound() {
        // Arrange
        when(workloadService.findTrainersByFullName("Unknown", "Person")).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = controller.searchTrainersByName("Unknown", "Person");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<TrainerWorkloadDocument> result = (List<TrainerWorkloadDocument>) response.getBody();

        assertTrue(result.isEmpty());

        verify(workloadService).findTrainersByFullName("Unknown", "Person");
    }

    @Test
    void searchTrainersByName_ExceptionHandling() {
        // Arrange
        when(workloadService.findTrainersByFullName("John", "Doe"))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<?> response = controller.searchTrainersByName("John", "Doe");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof String);
        assertTrue(response.getBody().toString().contains("Error:"));

        verify(workloadService).findTrainersByFullName("John", "Doe");
    }
}