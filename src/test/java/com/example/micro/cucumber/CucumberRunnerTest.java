package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Main Cucumber runner for all tests except integration tests.
 * Note: We're excluding integration tests to avoid duplicate step definitions.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.micro.cucumber.stepdefs"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json"
        },
        tags = "not @integration and not @integration-test"  // Exclude all integration tests
)
public class CucumberRunnerTest {
    // Empty class body
}
