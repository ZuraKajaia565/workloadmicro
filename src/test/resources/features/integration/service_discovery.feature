# src/test/resources/features/integration/service_discovery.feature
Feature: Service Discovery and Registration
  As a system administrator
  I want microservices to register with service discovery
  So that they can locate and communicate with each other

  Scenario: Workload service registers with Eureka
    Given the Eureka server is running
    When the Workload service starts up
    Then the Workload service should be registered with Eureka
    And the Workload service should be in UP status

  Scenario: Services can find each other via Eureka
    Given both Eureka and Workload services are running
    When a client requests information about available services
    Then the Workload service should be listed in the response