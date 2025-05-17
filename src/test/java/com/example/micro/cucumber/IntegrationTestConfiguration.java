package com.example.micro.cucumber;

// Remove this import
// import io.cucumber.spring.CucumberContextConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.example.micro.cucumber.config.IntegrationTestConfig;

// Remove this annotation
// @CucumberContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles("integration-test")
public class IntegrationTestConfiguration {
    // Configuration for integration tests
}