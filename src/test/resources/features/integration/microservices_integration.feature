Feature: Microservices Integration
  As a system administrator
  I want to ensure proper communication between microservices
  So that the system works as a cohesive unit

  Background:
    Given all microservices are running
    And the service discovery is operational
    And the message broker is running

  Scenario: Training session creation triggers notification
    Given a new training session is created
    When the workload service processes the session
    Then the notification service should receive the event
    And the trainer should be notified

  Scenario: Training session update propagates to all services
    Given an existing training session
    When the training session is updated
    Then all dependent services should be notified
    And their data should be synchronized

  Scenario: Service discovery and load balancing
    Given multiple instances of a service are running
    When a client makes a request
    Then the request should be properly routed
    And the response should be consistent

  Scenario: Circuit breaker pattern
    Given a service is experiencing high latency
    When multiple requests are made
    Then the circuit breaker should activate
    And fallback responses should be provided

  Scenario: Message queue reliability
    Given the message broker is temporarily unavailable
    When a service tries to send a message
    Then the message should be persisted
    And delivered when the broker is back online

  Scenario: Service health monitoring
    Given all services are running
    When the health check endpoint is called
    Then all services should report their status
    And the overall system health should be reported 