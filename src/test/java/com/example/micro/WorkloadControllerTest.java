package com.example.micro;

import com.example.micro.controller.WorkloadController;
import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadUpdateRequest;
import com.example.micro.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadController workloadController;

    private static final String USERNAME = "trainer1";
    private static final String TRANSACTION_ID = "test-transaction-id";

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void updateTrainerWorkload_WithTransactionId_Success() {
        // Arrange
        WorkloadUpdateRequest request = createWorkloadUpdateRequest();
        doNothing().when(workloadService).updateTrainerWorkload(any(WorkloadUpdateRequest.class));

        // Act
        ResponseEntity<String> response = workloadController.updateTrainerWorkload(request, TRANSACTION_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Workload updated successfully", response.getBody());
        verify(workloadService).updateTrainerWorkload(request);
    }

    @Test
    void updateTrainerWorkload_WithoutTransactionId_Success() {
        // Arrange
        WorkloadUpdateRequest request = createWorkloadUpdateRequest();
        doNothing().when(workloadService).updateTrainerWorkload(any(WorkloadUpdateRequest.class));

        // Act
        ResponseEntity<String> response = workloadController.updateTrainerWorkload(request, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Workload updated successfully", response.getBody());
        verify(workloadService).updateTrainerWorkload(request);
        // Would also verify a transaction ID was generated, but that's implementation-specific
    }

    @Test
    void getMonthlyWorkload_WithTransactionId_Success() {
        // Arrange
        int year = 2025;
        int month = 5;
        MonthlyWorkloadResponse expectedResponse = createMonthlyWorkloadResponse();

        when(workloadService.getMonthlyWorkload(USERNAME, year, month)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<MonthlyWorkloadResponse> response =
                workloadController.getMonthlyWorkload(USERNAME, year, month, TRANSACTION_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(workloadService).getMonthlyWorkload(USERNAME, year, month);
    }

    @Test
    void getMonthlyWorkload_WithoutTransactionId_Success() {
        // Arrange
        int year = 2025;
        int month = 5;
        MonthlyWorkloadResponse expectedResponse = createMonthlyWorkloadResponse();

        when(workloadService.getMonthlyWorkload(USERNAME, year, month)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<MonthlyWorkloadResponse> response =
                workloadController.getMonthlyWorkload(USERNAME, year, month, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(workloadService).getMonthlyWorkload(USERNAME, year, month);
    }

    @Test
    void getTrainerWorkloadSummary_WithTransactionId_Success() {
        // Arrange
        TrainerWorkloadResponse expectedResponse = createTrainerWorkloadResponse();

        when(workloadService.getTrainerWorkloadSummary(USERNAME)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<TrainerWorkloadResponse> response =
                workloadController.getTrainerWorkloadSummary(USERNAME, TRANSACTION_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(workloadService).getTrainerWorkloadSummary(USERNAME);
    }

    @Test
    void getTrainerWorkloadSummary_WithoutTransactionId_Success() {
        // Arrange
        TrainerWorkloadResponse expectedResponse = createTrainerWorkloadResponse();

        when(workloadService.getTrainerWorkloadSummary(USERNAME)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<TrainerWorkloadResponse> response =
                workloadController.getTrainerWorkloadSummary(USERNAME, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(workloadService).getTrainerWorkloadSummary(USERNAME);
    }

    // Helper methods
    private WorkloadUpdateRequest createWorkloadUpdateRequest() {
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2025, 5, 8));
        request.setTrainingDuration(60);
        request.setActionType(WorkloadUpdateRequest.ActionType.ADD);
        return request;
    }

    private MonthlyWorkloadResponse createMonthlyWorkloadResponse() {
        return new MonthlyWorkloadResponse(
                USERNAME,
                "John",
                "Doe",
                true,
                2025,
                5,
                60
        );
    }

    private TrainerWorkloadResponse createTrainerWorkloadResponse() {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setUsername(USERNAME);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setActive(true);
        return response;
    }
}