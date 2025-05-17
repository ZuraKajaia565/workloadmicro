package com.example.micro.cucumber.stepdefs.integration;

import com.example.micro.MicroApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring context configuration for integration tests.
 * This MUST be in the same package as the step definitions for integration tests.
 */

@SpringBootTest(
        classes = MicroApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration-test")
public class CucumberIntegrationConfiguration {
    // Empty class body - the annotations do all the work
}