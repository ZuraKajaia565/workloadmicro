# src/test/resources/features/workload_management.feature
Feature: Workload Management

  Scenario: Create new trainer workload
    Given a trainer with username "trainer1" exists
    When I create a workload with 60 minutes for month 5 of year 2025
    Then the workload is created successfully
    And the workload duration should be 60 minutes

  # Add your other scenarios...