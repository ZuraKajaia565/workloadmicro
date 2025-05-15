package com.example.micro.component.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.exception.ResourceNotFoundException;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class WorkloadManagementStepDefs {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    private String testUsername;
    private int testYear;
    private int testMonth;
    private int testDuration;
    private Exception thrownException;

    @Before
    public void setUp() {
        workloadRepository.deleteAll();
        thrownException = null;
    }

    @Given("a trainer with username {string} exists")
    public void aTrainerWithUsernameExists(String username) {
        testUsername = username;
        // Create a test trainer document
        TrainerWorkloadDocument trainer = new TrainerWorkloadDocument();
        trainer.setUsername(username);
        trainer.setFirstName("Test");
        trainer.setLastName("Trainer");
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());
        workloadRepository.save(trainer);
    }

    @When("I create a workload with {int} minutes for month {int} of year {int}")
    public void iCreateAWorkloadWithMinutesForMonthOfYear(int duration, int month, int year) {
        testDuration = duration;
        testMonth = month;
        testYear = year;

        workloadService.updateOrCreateWorkload(
                testUsername, testYear, testMonth, "Test", "Trainer", true, testDuration
        );
    }

    @Then("the workload is created successfully")
    public void theWorkloadIsCreatedSuccessfully() {
        TrainerWorkloadDocument trainer = workloadRepository.findById(testUsername).orElse(null);
        assertNotNull(trainer, "Trainer workload document should exist");

        // Further assertions to check if workload was created
        boolean found = false;
        for (TrainerWorkloadDocument.YearSummary year : trainer.getYears()) {
            if (year.getYear() == testYear) {
                for (TrainerWorkloadDocument.MonthSummary month : year.getMonths()) {
                    if (month.getMonth() == testMonth) {
                        found = true;
                        break;
                    }
                }
            }
        }
        assertTrue(found, "Workload entry should exist");
    }

    @Then("the workload duration should be {int} minutes")
    public void theWorkloadDurationShouldBeMinutes(int expectedDuration) {
        TrainerWorkloadDocument trainer = workloadRepository.findById(testUsername).orElse(null);
        assertNotNull(trainer, "Trainer workload document should exist");

        // Find the specific month and check duration
        boolean found = false;
        for (TrainerWorkloadDocument.YearSummary year : trainer.getYears()) {
            if (year.getYear() == testYear) {
                for (TrainerWorkloadDocument.MonthSummary month : year.getMonths()) {
                    if (month.getMonth() == testMonth) {
                        assertEquals(expectedDuration, month.getTrainingsSummaryDuration(),
                                "Workload duration should match expected value");
                        found = true;
                        break;
                    }
                }
            }
        }
        assertTrue(found, "Workload entry should exist with correct duration");
    }

    @Given("the trainer has a workload for month {int} of year {int} with {int} minutes")
    public void theTrainerHasAWorkloadForMonthOfYearWithMinutes(int month, int year, int duration) {
        testYear = year;
        testMonth = month;
        testDuration = duration;

        // Create the initial workload
        workloadService.updateOrCreateWorkload(
                testUsername, testYear, testMonth, "Test", "Trainer", true, testDuration
        );

        // Verify it was created
        TrainerWorkloadDocument trainer = workloadRepository.findById(testUsername).orElse(null);
        assertNotNull(trainer, "Trainer workload document should exist");
    }

    @When("I update the workload to {int} minutes")
    public void iUpdateTheWorkloadToMinutes(int newDuration) {
        testDuration = newDuration;

        workloadService.updateOrCreateWorkload(
                testUsername, testYear, testMonth, "Test", "Trainer", true, testDuration
        );
    }

    @Then("the workload is updated successfully")
    public void theWorkloadIsUpdatedSuccessfully() {
        // Similar to "the workload is created successfully" but specifically checking for updates
        TrainerWorkloadDocument trainer = workloadRepository.findById(testUsername).orElse(null);
        assertNotNull(trainer, "Trainer workload document should exist after update");
    }

    @When("I delete the workload")
    public void iDeleteTheWorkload() {
        try {
            workloadService.deleteWorkload(testUsername, testYear, testMonth);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the workload is deleted successfully")
    public void theWorkloadIsDeletedSuccessfully() {
        // No exception should have been thrown
        assertNull(thrownException, "No exception should be thrown during deletion");
    }

    @Then("the workload should not exist")
    public void theWorkloadShouldNotExist() {
        TrainerWorkloadDocument trainer = workloadRepository.findById(testUsername).orElse(null);

        if (trainer != null) {
            // If the trainer exists, make sure the specific month/year combination doesn't
            boolean found = false;
            for (TrainerWorkloadDocument.YearSummary year : trainer.getYears()) {
                if (year.getYear() == testYear) {
                    for (TrainerWorkloadDocument.MonthSummary month : year.getMonths()) {
                        if (month.getMonth() == testMonth) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            assertFalse(found, "Workload entry should not exist after deletion");
        }
        // If the trainer doesn't exist at all, the test passes
    }

    @When("I try to update a non-existent workload for month {int} of year {int}")
    public void iTryToUpdateANonExistentWorkloadForMonthOfYear(int month, int year) {
        testMonth = month;
        testYear = year;

        try {
            // Try to update a workload that doesn't exist yet
            // First make sure it doesn't exist (delete it if it does)
            try {
                workloadService.deleteWorkload(testUsername, testYear, testMonth);
            } catch (Exception ignored) {
                // Ignore any exception here, we just want to make sure it doesn't exist
            }

            // Now try to update a non-existent workload with negative duration
            // This should trigger a not found error
            workloadService.updateOrCreateWorkload(
                    testUsername, testYear, testMonth, "Test", "Trainer", true, -30
            );
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("I should receive a not found error")
    public void iShouldReceiveANotFoundError() {
        assertNotNull(thrownException, "An exception should have been thrown");
        assertTrue(thrownException instanceof ResourceNotFoundException,
                "Exception should be ResourceNotFoundException but was " + thrownException.getClass().getSimpleName());
    }
}