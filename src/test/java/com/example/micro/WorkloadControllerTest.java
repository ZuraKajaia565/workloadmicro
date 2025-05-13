package com.example.micro;

import com.example.micro.controller.WorkloadController;
import com.example.micro.document.TrainerWorkloadDocument;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadMongoService;

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
        TrainerWorkloadDocument.YearSummary yearSummary = new TrainerWorkloadDocument.YearSummary(2025);
        TrainerWorkloadDocument.MonthSummary monthSummary = new TrainerWorkloadDocument.MonthSummary(5, 60);
        yearSummary.getMonths().add(monthSummary);
        trainerDocument.getYears().add(yearSummary);
    }

    @Test
    void getTrainerWorkload_Found_Success() {
        // Arrange
        when(workloadMongoService.getTrainerWorkload("trainer1")).thenReturn(Optional.of(trainerDocument));

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
        assertEquals(60, result.getYears().get(0).getMonths().get(0).getSummaryDuration());

        verify(workloadMongoService).getTrainerWorkload("trainer1");
    }

    @Test
    void getTrainerWorkload_NotFound() {
        // Arrange
        when(workloadMongoService.getTrainerWorkload("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.getTrainerWorkload("nonexistent");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(workloadMongoService).getTrainerWorkload("nonexistent");
    }

    @Test
    void searchTrainersByName_Found_Success() {
        // Arrange
        List<TrainerWorkloadDocument> trainers = new ArrayList<>();
        trainers.add(trainerDocument);

        when(workloadMongoService.findTrainersByName("John", "Doe")).thenReturn(trainers);

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

        verify(workloadMongoService).findTrainersByName("John", "Doe");
    }

    @Test
    void searchTrainersByName_NotFound() {
        // Arrange
        when(workloadMongoService.findTrainersByName("Unknown", "Person")).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = controller.searchTrainersByName("Unknown", "Person");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(workloadMongoService).findTrainersByName("Unknown", "Person");
    }
}