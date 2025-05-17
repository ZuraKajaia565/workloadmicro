# src/test/resources/features/workload_management.feature
Feature: Trainer Workload Management
  As a gym manager
  I want to track trainer workloads
  So that I can optimize scheduling and monitor trainer utilization

  @component
  Scenario: Create new trainer workload
    Given a trainer with username "john.doe" exists
    When I create a workload with 60 minutes for month 5 of year 2025
    Then the workload is created successfully
    And the workload duration should be 60 minutes

  @component
  Scenario: Update existing trainer workload
    Given a trainer with username "jane.doe" exists
    And the trainer has a workload for month 6 of year 2025 with 30 minutes
    When I update the workload to 90 minutes
    Then the workload is updated successfully
    And the workload duration should be 90 minutes

  @component
  Scenario: Delete existing trainer workload
    Given a trainer with username "sam.smith" exists
    And the trainer has a workload for month 7 of year 2025 with 45 minutes
    When I delete the workload
    Then the workload is deleted successfully
    And the workload should not exist anymore

  @component
  Scenario: Attempt to retrieve non-existent trainer workload
    When I request workload for username "nonexistent"
    Then the response should be not found