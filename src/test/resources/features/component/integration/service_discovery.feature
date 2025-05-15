Feature: Service Discovery Integration

  Scenario: Workload service registers with Eureka server
    Given the Eureka server is running
    When the Workload service starts up
    Then the Workload service should be registered with Eureka
    And the Workload service should be in UP status

  Scenario: Trainer Service interacts with Workload Service through Service Discovery
    Given both Eureka and Workload services are running
    And a trainer with username "trainer1" exists in the system
    When a training with 60 minutes is added for the trainer
    Then the workload is successfully updated through service discovery
    And the workload information shows 60 minutes for the trainer
