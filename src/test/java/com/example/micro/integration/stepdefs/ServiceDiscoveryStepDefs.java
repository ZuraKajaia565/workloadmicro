package com.example.micro.integration.stepdefs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.micro.document.TrainerWorkloadDocument;
import com.example.micro.repository.TrainerWorkloadRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class ServiceDiscoveryStepDefs {

  @LocalServerPort private int port;

  @Autowired private TrainerWorkloadRepository workloadRepository;

  @Container
  private static final GenericContainer<?> eurekaServer =
      new GenericContainer<>("eureka-server:latest").withExposedPorts(8761);

  private String testUsername;
  private int testDuration;
  private int testYear = 2025;
  private int testMonth = 5;

  @DynamicPropertySource
  static void eurekaProperties(DynamicPropertyRegistry registry) {
    registry.add("eureka.client.serviceUrl.defaultZone",
                 ()
                     -> String.format("http://%s:%d/eureka/",
                                      eurekaServer.getHost(),
                                      eurekaServer.getMappedPort(8761)));
  }

  @Before
  public void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
    workloadRepository.deleteAll();
  }

  @Given("the Eureka server is running")
  public void theEurekaServerIsRunning() {
    assertTrue(eurekaServer.isRunning(), "Eureka server should be running");

    // Verify Eureka status endpoint
    given()
        .get("http://" + eurekaServer.getHost() + ":" +
             eurekaServer.getMappedPort(8761) + "/actuator/health")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @When("the Workload service starts up")
  public void theWorkloadServiceStartsUp() {
    // The service starts up automatically with SpringBootTest
    // Just verify it's running
    given()
        .get("/actuator/health")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @Then("the Workload service should be registered with Eureka")
  public void theWorkloadServiceShouldBeRegisteredWithEureka() {
    // Check Eureka for our registration
    Response response =
        given()
            .get("http://" + eurekaServer.getHost() + ":" +
                 eurekaServer.getMappedPort(8761) + "/eureka/apps")
            .then()
            .statusCode(200)
            .extract()
            .response();

    assertTrue(response.asString().contains("WORKLOAD-SERVICE"),
               "Workload service should be registered with Eureka");
  }

  @Then("the Workload service should be in UP status")
  public void theWorkloadServiceShouldBeInUPStatus() {
    Response response = given()
                            .get("http://" + eurekaServer.getHost() + ":" +
                                 eurekaServer.getMappedPort(8761) +
                                 "/eureka/apps/WORKLOAD-SERVICE")
                            .then()
                            .statusCode(200)
                            .extract()
                            .response();

    assertTrue(response.asString().contains("<status>UP</status>"),
               "Workload service should have UP status in Eureka");
  }

  @Given("both Eureka and Workload services are running")
  public void bothEurekaAndWorkloadServicesAreRunning() {
    theEurekaServerIsRunning();
    theWorkloadServiceStartsUp();
    theWorkloadServiceShouldBeRegisteredWithEureka();
  }

  @Given("a trainer with username {string} exists in the system")
  public void aTrainerWithUsernameExistsInTheSystem(String username) {
    testUsername = username;

    // Create trainer through API
    Map<String, Object> trainerData = new HashMap<>();
    trainerData.put("username", username);
    trainerData.put("firstName", "Test");
    trainerData.put("lastName", "Trainer");
    trainerData.put("isActive", true);

    given()
        .contentType(ContentType.JSON)
        .body(trainerData)
        .when()
        .post("/api/trainers")
        .then()
        .statusCode(anyOf(is(200), is(201)));
  }

  @When("a training with {int} minutes is added for the trainer")
  public void aTrainingWithMinutesIsAddedForTheTrainer(int duration) {
    testDuration = duration;

    // Create training through API
    Map<String, Object> trainingData = new HashMap<>();
    trainingData.put("username", testUsername);
    trainingData.put("year", testYear);
    trainingData.put("month", testMonth);
    trainingData.put("duration", testDuration);

    given()
        .contentType(ContentType.JSON)
        .body(trainingData)
        .when()
        .post("/api/trainings")
        .then()
        .statusCode(anyOf(is(200), is(201)));
  }

  @Then("the workload is successfully updated through service discovery")
  public void theWorkloadIsSuccessfullyUpdatedThroughServiceDiscovery() {
    // Check workload was updated through API
    Response response =
        given()
            .when()
            .get("/api/trainers/{username}/workloads/{year}/{month}",
                 testUsername, testYear, testMonth)
            .then()
            .statusCode(200)
            .extract()
            .response();

    assertNotNull(response.body(), "Response body should not be null");
  }

  @Then("the workload information shows {int} minutes for the trainer")
  public void
  theWorkloadInformationShowsMinutesForTheTrainer(int expectedDuration) {
    // Verify workload in database
    TrainerWorkloadDocument trainer =
        workloadRepository.findById(testUsername).orElse(null);
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
}
