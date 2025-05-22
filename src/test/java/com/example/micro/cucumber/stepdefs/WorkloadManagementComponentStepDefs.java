package com.example.micro.cucumber.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

public class WorkloadManagementComponentStepDefs {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkloadService workloadService;

    private AutoCloseable closeable;

    private String username;
    private int year;
    private int month;
    private int trainingDuration;
    private ResponseEntity<String> response;

    @Before
    public void setup() {
        // Initialize mocks before each scenario
        closeable = MockitoAnnotations.openMocks(this);
    }

    // Helper method to create headers
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Given("a trainer with username {string} exists")
    public void a_trainer_with_username_exists(String username) {
        this.username = username;

        // Create the test request payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("active", true);
        requestBody.put("trainingDuration", 0);

        // Make a real API request to create the trainer
        String url = "/api/trainers/" + username + "/workloads/2025/1";
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestBody, createHeaders()),
                    String.class
            );
        } catch (Exception e) {
            // If the trainer can't be created, the test will later fail
            System.out.println("Could not create trainer: " + e.getMessage());
        }
    }

    @When("I create a workload with {int} minutes for month {int} of year {int}")
    public void i_create_a_workload_with_minutes_for_month_of_year(Integer minutes, Integer month, Integer year) {
        this.month = month;
        this.year = year;
        this.trainingDuration = minutes;

        // Create workload request with the correct field names
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("active", true);
        requestBody.put("trainingDuration", minutes);

        // Make the actual API request
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );
    }

    @Then("the workload is created successfully")
    public void the_workload_is_created_successfully() {
        assertEquals(200, response.getStatusCodeValue(), "Expected 200 OK response");
    }

    @And("the workload duration should be {int} minutes")
    public void the_workload_duration_should_be_minutes(Integer expectedMinutes) {
        // Get monthly workload
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                Map.class
        );

        assertEquals(200, getResponse.getStatusCodeValue(), "Expected 200 OK response");
        Map<String, Object> responseMap = getResponse.getBody();
        assertEquals(expectedMinutes, responseMap.get("minutes"), "Workload duration should match");
    }

    @Given("the trainer has a workload for month {int} of year {int} with {int} minutes")
    public void the_trainer_has_a_workload_for_month_of_year_with_minutes(Integer month, Integer year, Integer minutes) {
        this.month = month;
        this.year = year;
        this.trainingDuration = minutes;

        // Create workload request with correct field names
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("active", true);
        requestBody.put("trainingDuration", minutes);

        // Make API request to create the workload
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        ResponseEntity<String> createResponse = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );

        assertEquals(200, createResponse.getStatusCodeValue(),
                "Expected 200 OK response when creating prerequisite workload");
    }

    @When("I update the workload to {int} minutes")
    public void i_update_the_workload_to_minutes(Integer newMinutes) {
        this.trainingDuration = newMinutes;

        // Create workload request with correct field names
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("active", true);
        requestBody.put("trainingDuration", newMinutes);

        // Make API request
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(requestBody, createHeaders()),
                String.class
        );
    }

    @Then("the workload is updated successfully")
    public void the_workload_is_updated_successfully() {
        assertEquals(200, response.getStatusCodeValue(), "Expected 200 OK response");
    }

    @When("I delete the workload")
    public void i_delete_the_workload() {
        // Make API request
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(createHeaders()),
                String.class
        );
    }

    @Then("the workload is deleted successfully")
    public void the_workload_is_deleted_successfully() {
        assertEquals(200, response.getStatusCodeValue(), "Expected 200 OK response");
    }

    @Then("the workload should not exist anymore")
    public void the_workload_should_not_exist_anymore() {
        // Get monthly workload
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                Map.class
        );

        assertEquals(404, getResponse.getStatusCodeValue(), "Expected 404 NOT_FOUND response");
    }

    @When("I request workload for username {string}")
    public void i_request_workload_for_username(String username) {
        this.username = username;

        // Make API request with error handling
        String url = "/api/trainers/" + username + "/workloads";
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    String.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Store the error response instead of letting it throw
            response = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }

    @Then("the response should be not found")
    public void the_response_should_be_not_found() {
        // Check if response is null and handle it
        if (response == null) {
            fail("Response is null - request may have failed completely");
        } else {
            assertEquals(404, response.getStatusCodeValue(), "Expected 404 NOT_FOUND response");
        }
    }

    @When("I try to update workload for username {string} for month {int} of year {int} in component test")
    public void i_try_to_update_workload_for_nonexistent_username(String username, Integer month, Integer year) {
        this.username = username;
        this.month = month;
        this.year = year;

        // Create workload request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("active", true);
        requestBody.put("trainingDuration", 60);

        // Make API request with error handling
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(requestBody, createHeaders()),
                    String.class
            );
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Store the error response instead of letting it throw
            response = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }
}
