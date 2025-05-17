# src/test/resources/features/workload_management.feature
Feature: Trainer Workload Management
  As a gym manager
  I want to track trainer workloads
  So that I can optimize scheduling and monitor trainer utilization

  Background:
    Given the workload service is running

  Scenario: Successfully create a new trainer workload
    Given a trainer with username "trainer1" exists
    When I create a workload with 60 minutes for month 5 of year 2025
    Then the workload is created successfully
    And the workload duration should be 60 minutes

  Scenario: Successfully update an existing trainer workload
    Given a trainer with username "trainer2" exists
    And the trainer has a workload for month 6 of year 2025 with 30 minutes
    When I update the workload to 90 minutes
    Then the workload is updated successfully
    And the workload duration should be 90 minutes

  Scenario: Successfully delete an existing trainer workload
    Given a trainer with username "trainer3" exists
    And the trainer has a workload for month 7 of year 2025 with 45 minutes
    When I delete the workload
    Then the workload is deleted successfully
    And the workload should not exist anymore

  Scenario: Return not found when trying to retrieve non-existent trainer workload
    When I request workload for username "nonexistent"
    Then the response should be not found

  Scenario: Return not found when trying to update non-existent trainer workload
    When I try to update workload for username "nonexistent" for month 8 of year 2025
    Then the response should be not found

  Scenario: Validation works for invalid workload duration
    Given a trainer with username "trainer4" exists
    When I try to create a workload with -10 minutes for month 9 of year 2025
    Then the request should be rejected with a validation error