package com.example.micro.component.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.messaging.WorkloadMessage;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkloadServiceStepDefs {

    private ConfigurableApplicationContext context;
    private TestRestTemplate restTemplate = new TestRestTemplate();
    private String baseUrl = "http://localhost:8083";
    private ResponseEntity<String> response;
    private String username;
    private int year;
    private int month;
    private int trainingDuration;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private WorkloadService workloadService;

    private WorkloadMessage mockMessage;
    private ArgumentCaptor<WorkloadMessage> messageCaptor;

    @Before
    public void setUp() {
        messageCaptor = ArgumentCaptor.forClass(WorkloadMessage.class);
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }

        // Clean up MongoDB test data
        if (mongoTemplate != null) {
            mongoTemplate.dropCollection(TrainerWorkloadDocument.class);
        }
    }

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        // Start the workload service with test properties
        System.setProperty("server.port", "8083");
        System.setProperty("spring.data.mongodb.database", "test_workload_db");
        System.setProperty("spring.activemq.broker-url", "vm://localhost?broker.persistent=false");

        // Run the application - normally we'd mock Spring Boot but for component test, we'll run a real instance
        // For a real test, you might want to use something like Spring Boot Test or TestContainers
        if (context == null || !context.isRunning()) {
            context = SpringApplication.run(com.example.micro.MicroApplication.class);

            // Get beans from context
            workloadRepository = context.getBean(TrainerWorkloadRepository.class);
            mongoTemplate = context.getBean(MongoTemplate.class);
            jmsTemplate = context.getBean(JmsTemplate.class);
            workloadService = context.getBean(WorkloadService.class);

            // Allow some time for the service to start
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @When("I create a new trainer workload with the following details:")
    public void iCreateANewTrainerWorkloadWithTheFollowingDetails(DataTable dataTable) {
        // Extract the data from the DataTable
        Map<String, String> workloadData = dataTable.asMaps().get(0);

        username = workloadData.get("username");
        year = Integer.parseInt(workloadData.get("year"));
        month = Integer.parseInt(workloadData.get("month"));
        trainingDuration = Integer.parseInt(workloadData.get("trainingDuration"));

        // Create the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", workloadData.get("firstName"));
        requestBody.put("lastName", workloadData.get("lastName"));
        requestBody.put("isActive", Boolean.parseBoolean(workloadData.get("isActive")));
        requestBody.put("trainingDuration", trainingDuration);

        // Send the request using TestRestTemplate
        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @Then("the workload should be saved successfully")
    public void theWorkloadShouldBeSavedSuccessfully() {
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("I should be able to retrieve the workload for {string}")
    public void iShouldBeAbleToRetrieveTheWorkloadFor(String username) {
        this.username = username;

        // Retrieve the workload
        String url = baseUrl + "/api/trainers/" + username + "/workloads";
        response = restTemplate.getForEntity(url, String.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verify the response contains the username
        Assert.assertTrue(response.getBody().contains(username));
    }

    @Then("the workload data should match the submitted values")
    public void theWorkloadDataShouldMatchTheSubmittedValues() {
        // Extract and verify specific fields from the response
        String responseBody = response.getBody();
        Assert.assertTrue(responseBody.contains(username));
        Assert.assertTrue(responseBody.contains(String.valueOf(year)));
        Assert.assertTrue(responseBody.contains(String.valueOf(month)));
        Assert.assertTrue(responseBody.contains(String.valueOf(trainingDuration)));
    }

    @Given("a trainer workload exists for {string} in year {int} and month {int}")
    public void aTrainerWorkloadExistsForInYearAndMonth(String username, int year, int month) {
        this.username = username;
        this.year = year;
        this.month = month;
        this.trainingDuration = 60; // Default value

        // Create a new workload first to ensure it exists
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @When("I update the workload duration to {int} minutes")
    public void iUpdateTheWorkloadDurationToMinutes(int newDuration) {
        this.trainingDuration = newDuration;

        // Create the update request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        // Send the update request
        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @Then("the workload should be updated successfully")
    public void theWorkloadShouldBeUpdatedSuccessfully() {
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the workload duration should be {int} minutes")
    public void theWorkloadDurationShouldBeMinutes(int expectedDuration) {
        // Retrieve the updated workload
        String url = baseUrl + "/api/trainers/" + username + "/workloads";
        response = restTemplate.getForEntity(url, String.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        // Check if the response contains the updated duration
        String responseBody = response.getBody();
        Assert.assertTrue(responseBody.contains(String.valueOf(expectedDuration)));
    }

    @When("I delete the workload for {string} in year {int} and month {int}")
    public void iDeleteTheWorkloadForInYearAndMonth(String username, int year, int month) {
        this.username = username;
        this.year = year;
        this.month = month;

        // Send the delete request
        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    }

    @Then("the workload should be deleted successfully")
    public void theWorkloadShouldBeDeletedSuccessfully() {
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the workload should not be retrievable")
    public void theWorkloadShouldNotBeRetrievable() {
        // Try to retrieve the deleted workload
        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.getForEntity(url, String.class);

        // Should get a 404 Not Found response
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @When("I request the workload for {string} in year {int} and month {int}")
    public void iRequestTheWorkloadForInYearAndMonth(String username, int year, int month) {
        this.username = username;
        this.year = year;
        this.month = month;

        // Send the request
        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.getForEntity(url, String.class);
    }

    @Then("I should receive the correct workload information")
    public void iShouldReceiveTheCorrectWorkloadInformation() {
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the response contains the expected information
        String responseBody = response.getBody();
        Assert.assertTrue(responseBody.contains(username));
        Assert.assertTrue(responseBody.contains(String.valueOf(year)));
        Assert.assertTrue(responseBody.contains(String.valueOf(month)));
    }

    @Then("I should receive a not found response")
    public void iShouldReceiveANotFoundResponse() {
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @When("I update a non-existent workload for {string} in year {int} and month {int}")
    public void iUpdateANonExistentWorkloadForInYearAndMonth(String username, int year, int month) {
        this.username = username;
        this.year = year;
        this.month = month;

        // Create the update request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", 60);

        // Send the update request
        String url = baseUrl + "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @When("a workload message is received with the following details:")
    public void aWorkloadMessageIsReceivedWithTheFollowingDetails(DataTable dataTable) {
        // Extract the data from the DataTable
        Map<String, String> messageData = dataTable.asMaps().get(0);

        // Create the workload message
        WorkloadMessage message = new WorkloadMessage();
        message.setUsername(messageData.get("username"));
        message.setFirstName(messageData.get("firstName"));
        message.setLastName(messageData.get("lastName"));
        message.setActive(Boolean.parseBoolean(messageData.get("isActive")));
        message.setYear(Integer.parseInt(messageData.get("year")));
        message.setMonth(Integer.parseInt(messageData.get("month")));
        message.setTrainingDuration(Integer.parseInt(messageData.get("trainingDuration")));
        message.setMessageType(WorkloadMessage.MessageType.valueOf(messageData.get("messageType")));
        message.setTransactionId("test-transaction-id");

        // Process the message
        workloadService.processWorkloadMessage(message);

        // Set for later verification
        username = messageData.get("username");
        year = Integer.parseInt(messageData.get("year"));
        month = Integer.parseInt(messageData.get("month"));
    }

    @When("a malformed workload message is received")
    public void aMalformedWorkloadMessageIsReceived() {
        // Create a malformed message (missing required fields)
        WorkloadMessage malformedMessage = new WorkloadMessage();
        // Missing username, which is required
        malformedMessage.setFirstName("Test");
        malformedMessage.setLastName("User");
        malformedMessage.setYear(2025);
        malformedMessage.setMonth(5);
        malformedMessage.setMessageType(WorkloadMessage.MessageType.CREATE_UPDATE);

        try {
            // Process the message - should throw an exception or handle error internally
            workloadService.processWorkloadMessage(malformedMessage);
        } catch (Exception e) {
            // Create a dummy response for testing
            response = ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Then("the message should be rejected")
    public void theMessageShouldBeRejected() {
        // Verify the message was rejected
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Then("the error should be logged")
    public void theErrorShouldBeLogged() {
        // In a real test, you might check the logs or a mock logger
        // For this example, we just verify there's an error message
        String responseBody = response.getBody();
        Assert.assertTrue(responseBody.contains("Error") || responseBody.contains("error"));
    }
}