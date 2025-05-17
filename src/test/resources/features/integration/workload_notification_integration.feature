# src/test/resources/features/integration/workload_notification_integration.feature
Feature: Workload Notification Integration
  As a system administrator
  I want workload updates to trigger notifications
  So that trainers are informed about their schedule changes

  Scenario: Workload update triggers notification
    Given the workload service and notification service are running
    And they are connected via ActiveMQ
    When a trainer workload is updated
    Then the notification service should receive the update event
    And a notification should be created for the trainer

  Scenario: Notification service handles workload deletion
    Given the workload service and notification service are running
    And a trainer has existing workload data
    When the workload is deleted
    Then the notification service should receive the deletion event
    And the trainer's schedule should be updated