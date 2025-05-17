package com.example.micro.cucumber;

import com.example.micro.cucumber.config.IntegrationTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Configuration for integration tests without the @CucumberContextConfiguration
 * annotation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles("integration-test")
public class IntegrationTestConfiguration {
  // Configuration for integration tests
}
