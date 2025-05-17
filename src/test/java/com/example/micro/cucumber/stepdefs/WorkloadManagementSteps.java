package com.example.micro.cucumber.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.service.WorkloadService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for workload management scenarios
 */
public class WorkloadManagementSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkloadService workloadService;

    private String username;
    private int year;
    private int month;
    private ResponseEntity<?> response;

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        // Verify service is running by checking health endpoint
        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(200, healthResponse.getStatusCodeValue());
    }

    @When("I request workload for username {string}")
    public void i_request_workload_for_username(String username) {
        this.username = username;
        response = restTemplate.getForEntity("/api/trainers/" + username + "/workloads", Object.class);
    }

    @Then("the response should be not found")
    public void the_response_should_be_not_found() {
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @When("I create a workload with {int} minutes for month {int} of year {int}")
    public void i_create_a_workload_with_minutes_for_month_of_year(Integer minutes, Integer month, Integer year) {
        this.month = month;
        this.year = year;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", minutes);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), Object.class);
    }

    @Then("the workload is created successfully")
    public void the_workload_is_created_successfully() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the workload duration should be {int} minutes")
    public void the_workload_duration_should_be_minutes(Integer minutes) {
        // Get workload from repository and verify duration
        TrainerWorkloadDocument workload = workloadService.getTrainerWorkload(username);

        for (TrainerWorkloadDocument.YearSummary yearSummary : workload.getYears()) {
            if (yearSummary.getYear() == year) {
                for (TrainerWorkloadDocument.MonthSummary monthSummary : yearSummary.getMonths()) {
                    if (monthSummary.getMonth() == month) {
                        assertEquals(minutes.intValue(), monthSummary.getTrainingsSummaryDuration());
                        return;
                    }
                }
            }
        }

        fail("Workload for the specified year and month not found");
    }

    @When("I update the workload to {int} minutes")
    public void i_update_the_workload_to_minutes(Integer minutes) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", minutes);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), Object.class);
    }

    @Then("the workload is updated successfully")
    public void the_workload_is_updated_successfully() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @When("I delete the workload")
    public void i_delete_the_workload() {
        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.DELETE, null, Object.class);
    }

    @Then("the workload is deleted successfully")
    public void the_workload_is_deleted_successfully() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Then("the workload should not exist anymore")
    public void the_workload_should_not_exist_anymore() {
        // Verify the workload no longer exists for this year and month
        TrainerWorkloadDocument workload = workloadService.getTrainerWorkload(username);

        for (TrainerWorkloadDocument.YearSummary yearSummary : workload.getYears()) {
            if (yearSummary.getYear() == year) {
                for (TrainerWorkloadDocument.MonthSummary monthSummary : yearSummary.getMonths()) {
                    if (monthSummary.getMonth() == month) {
                        fail("Workload still exists for year " + year + " and month " + month);
                    }
                }
            }
        }

        // If we get here, the workload doesn't exist, which is what we want
    }

    @When("I try to update workload for username {string} for month {int} of year {int}")
    public void i_try_to_update_workload_for_username_for_month_of_year(String username, Integer month, Integer year) {
        this.username = username;
        this.month = month;
        this.year = year;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", 60);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), Object.class);
    }

    @When("I try to create a workload with {int} minutes for month {int} of year {int}")
    public void i_try_to_create_a_workload_with_minutes_for_month_of_year(Integer minutes, Integer month, Integer year) {
        this.month = month;
        this.year = year;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("firstName", "Test");
        requestBody.put("lastName", "User");
        requestBody.put("isActive", true);
        requestBody.put("trainingDuration", minutes);

        String url = "/api/trainers/" + username + "/workloads/" + year + "/" + month;
        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestBody), Object.class);
    }

    @Then("the request should be rejected with a validation error")
    public void the_request_should_be_rejected_with_a_validation_error() {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}