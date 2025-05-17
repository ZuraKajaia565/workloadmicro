# src/test/resources/features/integration/message_flow.feature
Feature: Microservices Message Flow
  As a system architect
  I want to ensure that microservices communicate correctly
  So that data flows properly through the system

  @integration
  Scenario: Workload update triggers notification
    Given the workload service and notification service are running
    And they are connected via ActiveMQ
    And a trainer has existing workload data
    When a trainer workload is updated
    Then the notification service should receive the update event
    And a notification should be created for the trainer

  @integration
  Scenario: Workload deletion is propagated to other services
    Given the workload service and notification service are running
    And they are connected via ActiveMQ
    And a trainer has existing workload data
    When the workload is deleted
    Then the notification service should receive the deletion event
    And the trainer's schedule should be updated