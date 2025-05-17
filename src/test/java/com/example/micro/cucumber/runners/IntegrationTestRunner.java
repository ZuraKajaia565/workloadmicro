package com.example.micro.cucumber.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner for integration tests.
 * Note: We're using ONLY the com.example.micro.cucumber.stepdefs.integration package
 * to avoid duplicate step definitions.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/integration",
        glue = {"com.example.micro.cucumber.stepdefs.integration"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/integration-tests.html",
                "json:target/cucumber-reports/integration-tests.json"
        },
        tags = "@integration"  // Only run scenarios tagged with @integration
)
public class IntegrationTestRunner {
    // This class serves as a test runner for integration tests
}