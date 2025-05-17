// src/test/java/com/example/micro/cucumber/stepdefs/WorkloadManagementStepDefs.java
package com.example.micro.cucumber.stepdefs;

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
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class WorkloadManagementStepDefs {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String username;
    private int year;
    private int month;
    private int trainingDuration;
    private ResponseEntity<?> response;

    @Before
    public void setUp() {
        // Clean database before each scenario
        workloadRepository.deleteAll();
    }

    @After
    public void tearDown() {
        // Clean up after each scenario
        workloadRepository.deleteAll();
    }

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        // Check if application is running by making a health check request
        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
    }

    @Given("a trainer with username {string} exists")
    public void aTrainerWithUsernameExists(String username) {
        this.username = username;

        // Create a new trainer document
        TrainerWorkloadDocument trainer = new TrainerWorkloadDocument();
        trainer.setUsername(username);
        trainer.setFirstName("Test");
        trainer.setLastName("User");
        trainer.setActive(true);

        workloadRepository.save(trainer);
    }

    @Given("the trainer has a workload for month {int} of year {int} with {int} minutes")
    public void theTrainerHasAWorkloadForMonthOfYearWithMinutes(int month, int year, int minutes) {
        this.year = year;
        this.month = month;
        this.trainingDuration = minutes;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @When("I create a workload with {int} minutes for month {int} of year {int}")
    public void iCreateAWorkloadWithMinutesForMonthOfYear(int minutes, int month, int year) {
        this.year = year;
        this.month = month;
        this.trainingDuration = minutes;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @When("I update the workload to {int} minutes")
    public void iUpdateTheWorkloadToMinutes(int newDuration) {
        this.trainingDuration = newDuration;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @When("I delete the workload")
    public void iDeleteTheWorkload() {
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    }

    @When("I request workload for username {string}")
    public void iRequestWorkloadForUsername(String username) {
        String url = "/api/trainers/" + username + "/workloads";
        response = restTemplate.getForEntity(url, String.class);
    }

    @When("I try to update workload for username {string} for month {int} of year {int}")
    public void iTryToUpdateWorkloadForUsernameForMonthOfYear(String username, int month, int year) {
        this.username = username;
        this.year = year;
        this.month = month;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", 60);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @When("I try to create a workload with {int} minutes for month {int} of year {int}")
    public void iTryToCreateAWorkloadWithMinutesForMonthOfYear(int minutes, int month, int year) {
        this.year = year;
        this.month = month;
        this.trainingDuration = minutes;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @Then("the workload is created successfully")
    public void theWorkloadIsCreatedSuccessfully() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the workload is updated successfully")
    public void theWorkloadIsUpdatedSuccessfully() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the workload is deleted successfully")
    public void theWorkloadIsDeletedSuccessfully() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the response should be not found")
    public void theResponseShouldBeNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Then("the request should be rejected with a validation error")
    public void theRequestShouldBeRejectedWithAValidationError() {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @And("the workload duration should be {int} minutes")
    public void theWorkloadDurationShouldBeMinutes(int expectedDuration) {
        // Retrieve the workload
        String url = "/api/trainers/" + username + "/workloads";
        ResponseEntity<String> getResponse = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        // Verify duration in the response
        String responseBody = getResponse.getBody();
        assertTrue("Response should contain the expected duration",
                responseBody.contains("\"trainingsSummaryDuration\":" + expectedDuration) ||
                        responseBody.contains("\"summaryDuration\":" + expectedDuration));
    }

    @And("the workload should not exist anymore")
    public void theWorkloadShouldNotExistAnymore() {
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        ResponseEntity<String> getResponse = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}