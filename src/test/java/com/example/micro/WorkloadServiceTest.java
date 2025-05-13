package com.example.micro;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.messaging.WorkloadMessage;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository workloadRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private WorkloadService workloadService;

    @Captor
    private ArgumentCaptor<TrainerWorkloadDocument> trainerCaptor;

    private TrainerWorkloadDocument trainerDocument;
    private WorkloadMessage workloadMessage;

    @BeforeEach
    void setUp() {
        // Setup MDC with transaction ID
        MDC.put("transactionId", "test-transaction-id");

        // Create a sample trainer document
        trainerDocument = new TrainerWorkloadDocument();
        trainerDocument.setUsername("trainer1");
        trainerDocument.setFirstName("John");
        trainerDocument.setLastName("Doe");
        trainerDocument.setActive(true);
        trainerDocument.setYears(new ArrayList<>());

        // Create a sample WorkloadMessage
        workloadMessage = new WorkloadMessage();
        workloadMessage.setUsername("trainer1");
        workloadMessage.setFirstName("John");
        workloadMessage.setLastName("Doe");
        workloadMessage.setActive(true);
        workloadMessage.setYear(2025);
        workloadMessage.setMonth(5);
        workloadMessage.setTrainingDuration(60);
        workloadMessage.setMessageType(WorkloadMessage.MessageType.CREATE_UPDATE);
        workloadMessage.setTransactionId("test-transaction-id");
    }

    @Test
    void getTrainerWorkload_ExistingTrainer_ReturnsTrainer() {
        // Arrange
        when(workloadRepository.findById("trainer1")).thenReturn(Optional.of(trainerDocument));

        // Act
        TrainerWorkloadDocument result = workloadService.getTrainerWorkload("trainer1");

        // Assert
        assertNotNull(result);
        assertEquals("trainer1", result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.isActive());

        verify(workloadRepository).findById("trainer1");
    }

    @Test
    void getTrainerWorkload_NonExistingTrainer_ThrowsException() {
        // Arrange
        when(workloadRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            workloadService.getTrainerWorkload("nonexistent");
        });

        verify(workloadRepository).findById("nonexistent");
    }

    @Test
    void findTrainersByFullName_ExistingTrainers_ReturnsList() {
        // Arrange
        List<TrainerWorkloadDocument> trainers = new ArrayList<>();
        trainers.add(trainerDocument);

        when(workloadRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(trainers);

        // Act
        List<TrainerWorkloadDocument> result = workloadService.findTrainersByFullName("John", "Doe");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("trainer1", result.get(0).getUsername());

        verify(workloadRepository).findByFirstNameAndLastName("John", "Doe");
    }

    @Test
    void updateWorkloadAtomic_NewTrainer_CreatesTrainer() {
        // Arrange
        when(workloadRepository.existsById("trainer1")).thenReturn(false);

        // Act
        workloadService.updateWorkloadAtomic(workloadMessage);

        // Assert
        verify(workloadRepository).existsById("trainer1");
        verify(workloadRepository).save(trainerCaptor.capture());

        TrainerWorkloadDocument savedTrainer = trainerCaptor.getValue();
        assertEquals("trainer1", savedTrainer.getUsername());
        assertEquals("John", savedTrainer.getFirstName());
        assertEquals("Doe", savedTrainer.getLastName());
        assertTrue(savedTrainer.isActive());
        assertEquals(1, savedTrainer.getYears().size());
        assertEquals(2025, savedTrainer.getYears().get(0).getYear());
        assertEquals(1, savedTrainer.getYears().get(0).getMonths().size());
        assertEquals(5, savedTrainer.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(60, savedTrainer.getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());
    }



    @Test
    void processWorkloadMessage_CreateUpdateType_UpdatesWorkload() {
        // Arrange

        // Act
        workloadService.processWorkloadMessage(workloadMessage);

        // Assert
        // Verify that updateWorkloadAtomic was called with the correct message
        verify(workloadRepository).existsById("trainer1");
    }

    @Test
    void updateOrCreateWorkload_CallsUpdateWorkloadAtomic() {
        // Act
        workloadService.updateOrCreateWorkload(
                "trainer1", 2025, 5, "John", "Doe", true, 60);

        // Assert
        // Verify that method creates a WorkloadMessage and calls updateWorkloadAtomic
        verify(workloadRepository).existsById("trainer1");
    }
}