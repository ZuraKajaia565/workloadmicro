package com.example.micro.cucumber.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.service.WorkloadService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

        import java.util.HashMap;
import java.util.Map;

public class WorkloadManagementStepDefs {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TestRestTemplate restTemplate;

    private String username;
    private int year;
    private int month;
    private int trainingDuration;
    private ResponseEntity<String> response;

    @Given("a trainer with username {string} exists")
    public void aTrainerWithUsernameExists(String username) {
        this.username = username;

        // Create a trainer if not exists
        TrainerWorkloadDocument trainer = new TrainerWorkloadDocument();
        trainer.setUsername(username);
        trainer.setFirstName("Test");
        trainer.setLastName("User");
        trainer.setActive(true);

        try {
            // This might be a stub implementation for testing
            TrainerWorkloadDocument existing = workloadService.getTrainerWorkload(username);
            // Trainer exists, do nothing
        } catch (Exception e) {
            // Create a new trainer
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("firstName", "Test");
            requestBody.put("lastName", "User");
            requestBody.put("isActive", true);

            // Call the API to create trainer
            String url = "/api/trainers";
            response = restTemplate.postForEntity(url, requestBody, String.class);
        }
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

    @Then("the workload is created successfully")
    public void theWorkloadIsCreatedSuccessfully() {
        assertEquals(200, response.getStatusCodeValue(), "Workload should be created with status 200");
    }

    @And("the workload duration should be {int} minutes")
    public void theWorkloadDurationShouldBeMinutes(int expectedDuration) {
        // Retrieve the workload
        String url = "/api/trainers/" + username + "/workloads";
        ResponseEntity<String> getResponse = restTemplate.getForEntity(url, String.class);

        assertEquals(200, getResponse.getStatusCodeValue(), "Should get status 200");
        assertTrue(getResponse.getBody().contains(String.valueOf(expectedDuration)),
                "Response should contain the expected duration");
    }

    @Given("the trainer has a workload for month {int} of year {int} with {int} minutes")
    public void theTrainerHasAWorkloadForMonthOfYearWithMinutes(int month, int year, int minutes) {
        this.year = year;
        this.month = month;
        this.trainingDuration = minutes;

        // First ensure the workload exists
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", trainingDuration);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);

        assertEquals(200, response.getStatusCodeValue(), "Setup workload should be created with status 200");
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

    @Then("the workload is updated successfully")
    public void theWorkloadIsUpdatedSuccessfully() {
        assertEquals(200, response.getStatusCodeValue(), "Workload should be updated with status 200");
    }

    @When("I delete the workload")
    public void iDeleteTheWorkload() {
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    }

    @Then("the workload is deleted successfully")
    public void theWorkloadIsDeletedSuccessfully() {
        assertEquals(200, response.getStatusCodeValue(), "Workload should be deleted with status 200");
    }

    @And("the workload should not exist")
    public void theWorkloadShouldNotExist() {
        // Try to retrieve the deleted workload
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        ResponseEntity<String> getResponse = restTemplate.getForEntity(url, String.class);

        assertEquals(404, getResponse.getStatusCodeValue(), "Should get status 404 Not Found");
    }

    @When("I try to update a non-existent workload for month {int} of year {int}")
    public void iTryToUpdateANonExistentWorkloadForMonthOfYear(int month, int year) {
        this.year = year;
        this.month = month;

        // Create request for a workload that doesn't exist
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", 90);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), String.class);
    }

    @Then("I should receive a not found error")
    public void iShouldReceiveANotFoundError() {
        assertEquals(404, response.getStatusCodeValue(), "Should get status 404 Not Found");
    }
}