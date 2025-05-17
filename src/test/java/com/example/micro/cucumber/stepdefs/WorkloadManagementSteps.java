package com.example.micro.cucumber.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadManagementSteps {

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

        // Create workload using the service method
        workloadService.updateOrCreateWorkload(
                username, year, month, "Test", "User", true, trainingDuration
        );
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

    // Implement the rest of the step definitions...
    // Include methods for updating, deleting, and checking workloads
}