Feature: Workload Message Processing
  As a system administrator
  I want workload messages to be processed correctly
  So that trainer workload data stays consistent

  Background:
    Given the workload service is running with message processing enabled

  # Positive scenarios
  Scenario: Process a create workload message
    When a CREATE_UPDATE workload message is received with the following details:
      | username | firstName | lastName | isActive | year | month | trainingDuration |
      | trainer5 | John      | Doe      | true     | 2025 | 10    | 75               |
    Then the workload should be updated in the database
    And the workload message duration should be 75 minutes

  Scenario: Process a delete workload message
    Given a trainer with username "trainer6" exists
    And the trainer has a workload for month 11 of year 2025 with 50 minutes
    When a DELETE workload message is received for "trainer6" for month 11 of year 2025
    Then the workload should be deleted from the database

  # Negative scenario
  Scenario: Handle malformed workload message
    When a malformed workload message is received
    Then the message should be sent to the dead letter queue
    And an error log should be generated
