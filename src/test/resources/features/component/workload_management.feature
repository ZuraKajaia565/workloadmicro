Feature: Workload Management

  Scenario: Create new trainer workload
    Given a trainer with username "trainer1" exists
    When I create a workload with 60 minutes for month 5 of year 2025
    Then the workload is created successfully
    And the workload duration should be 60 minutes

  Scenario: Update existing trainer workload
    Given a trainer with username "trainer1" exists
    And the trainer has a workload for month 5 of year 2025 with 60 minutes
    When I update the workload to 90 minutes
    Then the workload is updated successfully
    And the workload duration should be 90 minutes

  Scenario: Delete existing trainer workload
    Given a trainer with username "trainer1" exists
    And the trainer has a workload for month 5 of year 2025 with 60 minutes
    When I delete the workload
    Then the workload is deleted successfully
    And the workload should not exist

  Scenario: Attempt to update non-existent workload
    Given a trainer with username "trainer1" exists
    When I try to update a non-existent workload for month 6 of year 2025
    Then I should receive a not found error