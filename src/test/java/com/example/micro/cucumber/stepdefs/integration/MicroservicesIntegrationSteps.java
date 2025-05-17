package com.example.micro.cucumber.stepdefs.integration;

import com.example.micro.config.JmsConfig;
import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class MicroservicesIntegrationSteps {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String username;
    private int year = 2025;
    private int month = 10;
    private int trainingDuration = 60;

    private AtomicReference<Message<?>> receivedMessage = new AtomicReference<>();
    private CountDownLatch messageLatch = new CountDownLatch(1);

    @Before
    public void setUp() {
        workloadRepository.deleteAll();
        receivedMessage.set(null);
        messageLatch = new CountDownLatch(1);

        // Set up message listener to simulate notification service
        setupMessageListener();
    }

    @After
    public void tearDown() {
        workloadRepository.deleteAll();
    }

    @Given("the workload service and notification service are running")
    public void theWorkloadServiceAndNotificationServiceAreRunning() {
        // Check workload service health
        ResponseEntity<String> workloadHealth = restTemplate.getForEntity(
                "/actuator/health", String.class);
        assertEquals(200, workloadHealth.getStatusCodeValue());
    }

    // Implement the rest of the step definitions...
    // Include methods for message processing, checking notifications, etc.

    /**
     * Sets up a JMS message listener to simulate the notification service
     */
    private void setupMessageListener() {
        // Start a background thread to listen for messages
        new Thread(() -> {
            try {
                // Listen for messages on the workload queue
                jakarta.jms.Message message = jmsTemplate.receive(JmsConfig.WORKLOAD_QUEUE);
                if (message != null) {
                    // Store the received message for verification
                    @SuppressWarnings("unchecked")
                    Message<Object> springMessage = (Message<Object>) message;
                    receivedMessage.set(springMessage);
                    messageLatch.countDown();
                }
            } catch (Exception e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }).start();
    }
}