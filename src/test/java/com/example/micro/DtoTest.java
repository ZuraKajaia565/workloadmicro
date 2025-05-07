package com.example.micro;

import com.example.micro.dto.MonthlyWorkloadResponse;
import com.example.micro.dto.TrainerWorkloadResponse;
import com.example.micro.dto.WorkloadUpdateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DtoTest {

    @Test
    void monthlyWorkloadResponse_Constructor_GettersWork() {
        // Arrange & Act
        String username = "john.doe";
        String firstName = "John";
        String lastName = "Doe";
        boolean isActive = true;
        int year = 2025;
        int month = 5;
        int duration = 60;

        MonthlyWorkloadResponse response = new MonthlyWorkloadResponse(
                username, firstName, lastName, isActive, year, month, duration);

        // Assert
        assertEquals(username, response.getUsername());
        assertEquals(firstName, response.getFirstName());
        assertEquals(lastName, response.getLastName());
        assertEquals(isActive, response.isActive());
        assertEquals(year, response.getYear());
        assertEquals(month, response.getMonth());
        assertEquals(duration, response.getSummaryDuration());
    }

    @Test
    void monthlyWorkloadResponse_NoArgsConstructor_GettersSettersWork() {
        // Arrange
        MonthlyWorkloadResponse response = new MonthlyWorkloadResponse();

        // Act
        response.setUsername("john.doe");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setActive(true);
        response.setYear(2025);
        response.setMonth(5);
        response.setSummaryDuration(60);

        // Assert
        assertEquals("john.doe", response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.isActive());
        assertEquals(2025, response.getYear());
        assertEquals(5, response.getMonth());
        assertEquals(60, response.getSummaryDuration());
    }

    @Test
    void trainerWorkloadResponse_GettersSettersWork() {
        // Arrange
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();

        // Act
        response.setUsername("john.doe");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setActive(true);

        TrainerWorkloadResponse.YearSummaryDto yearDto = new TrainerWorkloadResponse.YearSummaryDto();
        yearDto.setYear(2025);

        TrainerWorkloadResponse.MonthSummaryDto monthDto = new TrainerWorkloadResponse.MonthSummaryDto();
        monthDto.setMonth(5);
        monthDto.setSummaryDuration(60);

        yearDto.getMonths().add(monthDto);
        response.getYears().add(yearDto);

        // Assert
        assertEquals("john.doe", response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.isActive());
        assertEquals(1, response.getYears().size());
        assertEquals(2025, response.getYears().get(0).getYear());
        assertEquals(1, response.getYears().get(0).getMonths().size());
        assertEquals(5, response.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(60, response.getYears().get(0).getMonths().get(0).getSummaryDuration());
    }

    @Test
    void yearSummaryDto_ConstructorAndGettersSettersWork() {
        // Arrange & Act with Constructor
        TrainerWorkloadResponse.YearSummaryDto yearDtoWithConstructor =
                new TrainerWorkloadResponse.YearSummaryDto(2025);

        // Assert Constructor
        assertEquals(2025, yearDtoWithConstructor.getYear());

        // Arrange & Act with No-Args Constructor and Setters
        TrainerWorkloadResponse.YearSummaryDto yearDto = new TrainerWorkloadResponse.YearSummaryDto();
        yearDto.setYear(2026);

        // Assert Getters and Setters
        assertEquals(2026, yearDto.getYear());
        assertNotNull(yearDto.getMonths());
        assertTrue(yearDto.getMonths().isEmpty());
    }

    @Test
    void monthSummaryDto_ConstructorAndGettersSettersWork() {
        // Arrange & Act with Constructor
        TrainerWorkloadResponse.MonthSummaryDto monthDtoWithConstructor =
                new TrainerWorkloadResponse.MonthSummaryDto(5, 60);

        // Assert Constructor
        assertEquals(5, monthDtoWithConstructor.getMonth());
        assertEquals(60, monthDtoWithConstructor.getSummaryDuration());

        // Arrange & Act with No-Args Constructor and Setters
        TrainerWorkloadResponse.MonthSummaryDto monthDto = new TrainerWorkloadResponse.MonthSummaryDto();
        monthDto.setMonth(6);
        monthDto.setSummaryDuration(120);

        // Assert Getters and Setters
        assertEquals(6, monthDto.getMonth());
        assertEquals(120, monthDto.getSummaryDuration());
    }

    @Test
    void workloadUpdateRequest_AllArgsConstructor_GettersSettersWork() {
        // Arrange
        String username = "john.doe";
        String firstName = "John";
        String lastName = "Doe";
        boolean isActive = true;
        LocalDate trainingDate = LocalDate.of(2025, 5, 8);
        int trainingDuration = 60;
        WorkloadUpdateRequest.ActionType actionType = WorkloadUpdateRequest.ActionType.ADD;

        // Act with Constructor
        WorkloadUpdateRequest request = new WorkloadUpdateRequest(
                username, firstName, lastName, isActive, trainingDate, trainingDuration, actionType);

        // Assert Constructor and Getters
        assertEquals(username, request.getUsername());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertTrue(request.isActive());
        assertEquals(trainingDate, request.getTrainingDate());
        assertEquals(trainingDuration, request.getTrainingDuration());
        assertEquals(actionType, request.getActionType());

        // Act with No-Args Constructor and Setters
        WorkloadUpdateRequest emptyRequest = new WorkloadUpdateRequest();
        emptyRequest.setUsername("jane.doe");
        emptyRequest.setFirstName("Jane");
        emptyRequest.setLastName("Doe");
        emptyRequest.setActive(false);
        emptyRequest.setTrainingDate(LocalDate.of(2025, 6, 10));
        emptyRequest.setTrainingDuration(90);
        emptyRequest.setActionType(WorkloadUpdateRequest.ActionType.DELETE);

        // Assert Getters and Setters
        assertEquals("jane.doe", emptyRequest.getUsername());
        assertEquals("Jane", emptyRequest.getFirstName());
        assertEquals("Doe", emptyRequest.getLastName());
        assertFalse(emptyRequest.isActive());
        assertEquals(LocalDate.of(2025, 6, 10), emptyRequest.getTrainingDate());
        assertEquals(90, emptyRequest.getTrainingDuration());
        assertEquals(WorkloadUpdateRequest.ActionType.DELETE, emptyRequest.getActionType());
    }

    @Test
    void workloadUpdateRequest_ActionTypeEnum_HasCorrectValues() {
        // Assert
        assertEquals(2, WorkloadUpdateRequest.ActionType.values().length);
        assertEquals(WorkloadUpdateRequest.ActionType.ADD, WorkloadUpdateRequest.ActionType.valueOf("ADD"));
        assertEquals(WorkloadUpdateRequest.ActionType.DELETE, WorkloadUpdateRequest.ActionType.valueOf("DELETE"));
    }
}