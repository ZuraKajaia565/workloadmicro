# src/test/resources/features/integration/message_flow.feature
@integration
Feature: Microservices Message Flow
  As a system architect
  I want to ensure that microservices communicate correctly
  So that data flows properly through the system

  Scenario: Workload update triggers notification
    Given the workload and notification services are running
    And they are connected via ActiveMQ
    And a trainer has existing workload data
    When a trainer workload is updated
    Then the notification service should receive the update event
    And a notification should be created for the trainer

  # ... more scenarios ...