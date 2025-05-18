package com.example.micro.cucumber.stepdefs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.micro.service.WorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class WorkloadManagementComponentStepDefs {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private ObjectMapper objectMapper;

    private String username;
    private Integer year;
    private Integer month;
    private Integer minutes;
    private ResponseEntity<String> response;

    // Helper method to create headers with authentication
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer mock-jwt-token");  // Add JWT token header
        return headers;
    }

    @Given("a trainer with username {string} exists")
    public void a_trainer_with_username_exists(String username) {
        this.username = username;
        // In test environment, we can assume the trainer exists
    }

    @When("I create a workload with {int} minutes for month {int} of year {int}")
    public void i_create_a_workload_with_minutes_for_month_of_year(Integer minutes, Integer month, Integer year) {
        this.month = month;
        this.year = year;
        this.minutes = minutes;

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("minutes", minutes);

        // Make the request WITH AUTH HEADERS
        response = restTemplate.exchange(
                "/api/trainers/{username}/workloads/{year}/{month}",
                HttpMethod.PUT,
                new HttpEntity<>(requestBody, createHeaders()),  // Add headers here
                String.class,
                username, year, month
        );
    }

    @Given("the trainer has a workload for month {int} of year {int} with {int} minutes")
    public void the_trainer_has_a_workload_for_month_of_year_with_minutes(Integer month, Integer year, Integer minutes) {
        this.month = month;
        this.year = year;
        this.minutes = minutes;

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("minutes", minutes);

        // Create or update the workload directly via API
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/trainers/{username}/workloads/{year}/{month}",
                HttpMethod.PUT,
                new HttpEntity<>(requestBody, createHeaders()),  // Add headers here
                String.class,
                username, year, month
        );

        // Verify it was created
        assertEquals(200, createResponse.getStatusCodeValue(), "Expected 200 OK response when creating prerequisite workload");

        // Verify workload exists
        assertTrue(workloadService.workloadExists(username, year, month),
                "Workload should exist for specified year and month");
    }

    @When("I update the workload to {int} minutes")
    public void i_update_the_workload_to_minutes(Integer newMinutes) {
        this.minutes = newMinutes;

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("minutes", newMinutes);

        // Make the request WITH AUTH HEADERS
        response = restTemplate.exchange(
                "/api/trainers/{username}/workloads/{year}/{month}",
                HttpMethod.PUT,
                new HttpEntity<>(requestBody, createHeaders()),  // Add headers here
                String.class,
                username, year, month
        );
    }

    @When("I delete the workload")
    public void i_delete_the_workload() {
        // Make the request WITH AUTH HEADERS
        response = restTemplate.exchange(
                "/api/trainers/{username}/workloads/{year}/{month}",
                HttpMethod.DELETE,
                new HttpEntity<>(createHeaders()),  // Add headers here
                String.class,
                username, year, month
        );
    }

    @When("I request workload for username {string}")
    public void i_request_workload_for_username(String username) {
        this.username = username;

        // Make the request WITH AUTH HEADERS
        response = restTemplate.exchange(
                "/api/trainers/{username}/workloads",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),  // Add headers here
                String.class,
                username
        );
    }

    // Remaining methods - update to use createHeaders() where needed

    @Then("the workload is created successfully")
    public void the_workload_is_created_successfully() {
        assertEquals(200, response.getStatusCodeValue(), "Expected 200 OK response");
    }

    @Then("the workload is updated successfully")
    public void the_workload_is_updated_successfully() {
        assertEquals(200, response.getStatusCodeValue(), "Expected 200 OK response");
    }

    @Then("the workload is deleted successfully")
    public void the_workload_is_deleted_successfully() {
        assertEquals(204, response.getStatusCodeValue(), "Expected 204 NO_CONTENT response");
    }

    @Then("the workload duration should be {int} minutes")
    public void the_workload_duration_should_be_minutes(Integer expectedMinutes) {
        // Get the workload WITH AUTH HEADERS
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/api/trainers/{username}/workloads/{year}/{month}",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),  // Add headers here
                String.class,
                username, year, month
        );

        assertEquals(200, getResponse.getStatusCodeValue(), "Expected 200 OK response");

        try {
            Map<String, Object> responseMap = objectMapper.readValue(getResponse.getBody(), Map.class);
            assertEquals(expectedMinutes, responseMap.get("minutes"), "Workload duration should match expected minutes");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    @Then("the workload should not exist anymore")
    public void the_workload_should_not_exist_anymore() {
        // Try to get the deleted workload WITH AUTH HEADERS
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/api/trainers/{username}/workloads/{year}/{month}",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),  // Add headers here
                String.class,
                username, year, month
        );

        assertEquals(404, getResponse.getStatusCodeValue(), "Expected 404 NOT_FOUND response");
    }

    @Then("the response should be not found")
    public void the_response_should_be_not_found() {
        assertEquals(404, response.getStatusCodeValue(), "Expected 404 NOT_FOUND response");
    }
}