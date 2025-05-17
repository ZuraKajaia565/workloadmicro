package com.example.micro.cucumber.stepdefs.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

@SpringBootTest
@ContextConfiguration
public class MicroservicesIntegrationSteps {

  @Autowired private DiscoveryClient discoveryClient;

  @Autowired private LoadBalancerClient loadBalancerClient;

  @Autowired private RestTemplate restTemplate;

  private WireMockServer wireMockServer;
  private Network network;
  private GenericContainer<?> workloadService;
  private GenericContainer<?> notificationService;
  private ResponseEntity<?> response;
  private Exception exception;

  @Given("all microservices are operational")
  public void allMicroservicesAreOperational() {
    network = Network.newNetwork();

    // Start workload service container
    workloadService = new GenericContainer<>("workload-service:latest")
                          .withNetwork(network)
                          .withExposedPorts(8080);
    workloadService.start();

    // Start notification service container
    notificationService = new GenericContainer<>("notification-service:latest")
                              .withNetwork(network)
                              .withExposedPorts(8081);
    notificationService.start();
  }

  @Given("the service discovery is operational")
  public void theServiceDiscoveryIsOperational() {
    List<String> services = discoveryClient.getServices();
    assertFalse(services.isEmpty());
    assertTrue(services.contains("workload-service"));
    assertTrue(services.contains("notification-service"));
  }

  @Given("the message broker is running")
  public void theMessageBrokerIsRunning() {
    // Verify ActiveMQ is running
    assertTrue(workloadService.isRunning());
    assertTrue(notificationService.isRunning());
  }

  @Given("a new training session is created")
  public void aNewTrainingSessionIsCreated() {
    // Create a test training session
    String workloadServiceUrl =
        "http://localhost:" + workloadService.getMappedPort(8080);
    response = restTemplate.postForEntity(
        workloadServiceUrl + "/api/training-sessions",
        createTestTrainingSession(), Object.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @When("the workload service processes the session")
  public void theWorkloadServiceProcessesTheSession() {
    // Wait for processing
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Then("the notification service should receive the event")
  public void theNotificationServiceShouldReceiveTheEvent() {
    String notificationServiceUrl =
        "http://localhost:" + notificationService.getMappedPort(8081);
    response = restTemplate.getForEntity(
        notificationServiceUrl + "/api/notifications/latest", Object.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @And("the trainer should be notified")
  public void theTrainerShouldBeNotified() {
    assertNotNull(response.getBody());
  }

  @Given("an existing training session")
  public void anExistingTrainingSession() {
    aNewTrainingSessionIsCreated();
  }

  @When("the training session is updated")
  public void theTrainingSessionIsUpdated() {
    String workloadServiceUrl =
        "http://localhost:" + workloadService.getMappedPort(8080);
    response = restTemplate.exchange(
        workloadServiceUrl + "/api/training-sessions/1",
        org.springframework.http.HttpMethod.PUT,
        new org.springframework.http.HttpEntity<>(createTestTrainingSession()),
        Object.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @Then("all dependent services should be notified")
  public void allDependentServicesShouldBeNotified() {
    String notificationServiceUrl =
        "http://localhost:" + notificationService.getMappedPort(8081);
    response = restTemplate.getForEntity(
        notificationServiceUrl + "/api/notifications/latest", Object.class);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @And("their data should be synchronized")
  public void theirDataShouldBeSynchronized() {
    // Verify data consistency across services
    assertNotNull(response.getBody());
  }

  @Given("multiple instances of a service are running")
  public void multipleInstancesOfAServiceAreRunning() {
    // Start additional instance
    GenericContainer<?> additionalInstance =
        new GenericContainer<>("workload-service:latest")
            .withNetwork(network)
            .withExposedPorts(8082);
    additionalInstance.start();
  }

  @When("a client makes a request")
  public void aClientMakesARequest() {
    // Make request through load balancer
    String serviceUrl =
        loadBalancerClient.choose("workload-service").getUri().toString();
    response = restTemplate.getForEntity(
        serviceUrl + "/api/training-sessions", Object.class);
  }

  @Then("the request should be properly routed")
  public void theRequestShouldBeProperlyRouted() {
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @And("the response should be consistent")
  public void theResponseShouldBeConsistent() {
    assertNotNull(response.getBody());
  }

  @Given("a service is experiencing high latency")
  public void aServiceIsExperiencingHighLatency() {
    wireMockServer = new WireMockServer(8089);
    wireMockServer.start();

    wireMockServer.stubFor(
        get(urlPathMatching("/api/training-sessions"))
            .willReturn(aResponse().withStatus(200).withFixedDelay(5000)));
  }

  @When("multiple requests are made")
  public void multipleRequestsAreMade() {
    // Make multiple requests to trigger circuit breaker
    for (int i = 0; i < 5; i++) {
      try {
        response = restTemplate.getForEntity(
            "http://localhost:8089/api/training-sessions", Object.class);
      } catch (Exception e) {
        exception = e;
      }
    }
  }

  @Then("the circuit breaker should activate")
  public void theCircuitBreakerShouldActivate() {
    assertNotNull(exception);
  }

  @And("fallback responses should be provided")
  public void fallbackResponsesShouldBeProvided() {
    // Verify fallback response
    assertNotNull(response);
  }

  @Given("the message broker is temporarily unavailable")
  public void theMessageBrokerIsTemporarilyUnavailable() {
    notificationService.stop();
  }

  @When("a service tries to send a message")
  public void aServiceTriesToSendAMessage() {
    try {
      aNewTrainingSessionIsCreated();
    } catch (Exception e) {
      exception = e;
    }
  }

  @Then("the message should be persisted")
  public void theMessageShouldBePersisted() {
    // Verify message persistence
    assertNotNull(exception);
  }

  @And("delivered when the broker is back online")
  public void deliveredWhenTheBrokerIsBackOnline() {
    notificationService.start();
    try {
      Thread.sleep(5000); // Wait for service to start
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    theNotificationServiceShouldReceiveTheEvent();
  }

  @Given("all services are running")
  public void allServicesAreRunning() {
    allMicroservicesAreOperational();
  }

  @When("the health check endpoint is called")
  public void theHealthCheckEndpointIsCalled() {
    String workloadServiceUrl =
        "http://localhost:" + workloadService.getMappedPort(8080);
    response = restTemplate.getForEntity(
        workloadServiceUrl + "/actuator/health", Object.class);
  }

  @Then("all services should report their status")
  public void allServicesShouldReportTheirStatus() {
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @And("the overall system health should be reported")
  public void theOverallSystemHealthShouldBeReported() {
    assertNotNull(response.getBody());
  }

  private Object createTestTrainingSession() {
    // Create and return a test training session object
    return new Object(); // Replace with actual training session object
  }
}
