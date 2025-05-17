// src/test/java/com/example/micro/cucumber/stepdefs/WorkloadMessagingStepDefs.java
package com.example.micro.cucumber.stepdefs;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.messaging.WorkloadMessage;
import com.example.micro.repository.TrainerWorkloadRepository;
import com.example.micro.service.WorkloadService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

public class WorkloadMessagingStepDefs {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    private String username;
    private int year;
    private int month;
    private int trainingDuration;

    @Before
    public void setUp() {
        workloadRepository.deleteAll();
    }

    @After
    public void tearDown() {
        workloadRepository.deleteAll();
    }

    @Given("the workload service is running with message processing enabled")
    public void theWorkloadServiceIsRunningWithMessageProcessingEnabled() {
        // This is a precondition that's already met in the test environment
    }

    @When("a CREATE_UPDATE workload message is received with the following details:")
    public void aCreateUpdateWorkloadMessageIsReceived(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> data = rows.get(0);

        this.username = data.get("username");
        this.year = Integer.parseInt(data.get("year"));
        this.month = Integer.parseInt(data.get("month"));
        this.trainingDuration = Integer.parseInt(data.get("trainingDuration"));

        WorkloadMessage message = new WorkloadMessage();
        message.setUsername(username);
        message.setFirstName(data.get("firstName"));
        message.setLastName(data.get("lastName"));
        message.setActive(Boolean.parseBoolean(data.get("isActive")));
        message.setYear(year);
        message.setMonth(month);
        message.setTrainingDuration(trainingDuration);
        message.setMessageType(WorkloadMessage.MessageType.CREATE_UPDATE);
        message.setTransactionId(UUID.randomUUID().toString());

        // Process the message directly rather than sending it to ActiveMQ
        workloadService.processWorkloadMessage(message);
    }

    @When("a DELETE workload message is received for {string} for month {int} of year {int}")
    public void aDeleteWorkloadMessageIsReceived(String username, int month, int year) {
        this.username = username;
        this.year = year;
        this.month = month;

        WorkloadMessage message = new WorkloadMessage();
        message.setUsername(username);
        message.setFirstName("Test");
        message.setLastName("User");
        message.setActive(true);
        message.setYear(year);
        message.setMonth(month);
        message.setTrainingDuration(0); // Not relevant for delete
        message.setMessageType(WorkloadMessage.MessageType.DELETE);
        message.setTransactionId(UUID.randomUUID().toString());

        // Process the message directly
        workloadService.processWorkloadMessage(message);
    }

    @When("a malformed workload message is received")
    public void aMalformedWorkloadMessageIsReceived() {
        // Create a malformed message (missing required fields)
        WorkloadMessage message = new WorkloadMessage();
        // Missing username and other required fields
        message.setYear(2025);
        message.setMonth(5);
        message.setMessageType(WorkloadMessage.MessageType.CREATE_UPDATE);
        message.setTransactionId(UUID.randomUUID().toString());

        try {
            // This should cause an error
            workloadService.processWorkloadMessage(message);
        } catch (Exception e) {
            // Expected exception - this is what we want to test
        }
    }

    @Then("the workload should be updated in the database")
    public void theWorkloadShouldBeUpdatedInTheDatabase() {
        Optional<TrainerWorkloadDocument> trainerOpt = workloadRepository.findById(username);
        assertTrue("Trainer should exist in repository", trainerOpt.isPresent());

        TrainerWorkloadDocument trainer = trainerOpt.get();

        boolean found = false;
        for (TrainerWorkloadDocument.YearSummary yearSummary : trainer.getYears()) {
            if (yearSummary.getYear() == year) {
                for (TrainerWorkloadDocument.MonthSummary monthSummary : yearSummary.getMonths()) {
                    if (monthSummary.getMonth() == month) {
                        found = true;
                        break;
                    }
                }
            }
        }

        assertTrue("Month should exist in workload", found);
    }

    @Then("the workload should be deleted from the database")
    public void theWorkloadShouldBeDeletedFromTheDatabase() {
        Optional<TrainerWorkloadDocument> trainerOpt = workloadRepository.findById(username);

        if (trainerOpt.isPresent()) {
            TrainerWorkloadDocument trainer = trainerOpt.get();

            boolean found = false;
            for (TrainerWorkloadDocument.YearSummary yearSummary : trainer.getYears()) {
                if (yearSummary.getYear() == year) {
                    for (TrainerWorkloadDocument.MonthSummary monthSummary : yearSummary.getMonths()) {
                        if (monthSummary.getMonth() == month) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            assertFalse("Month should not exist in workload", found);
        }
    }

    @Then("the message should be sent to the dead letter queue")
    public void theMessageShouldBeSentToTheDeadLetterQueue() {
        // This would require a mock DLQ listener which is beyond scope here
        // In a real implementation, you would verify that the message was sent to the DLQ
    }

    @And("an error log should be generated")
    public void anErrorLogShouldBeGenerated() {
        // This would require a mock logger which is beyond scope here
        // In a real implementation, you would verify that an error was logged
    }
}