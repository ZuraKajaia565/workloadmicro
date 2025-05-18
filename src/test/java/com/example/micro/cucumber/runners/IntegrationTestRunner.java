// src/test/java/com/example/micro/cucumber/runners/IntegrationTestRunner.java
package com.example.micro.cucumber.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/integration",
        glue = {"com.example.micro.cucumber.integration", "com.example.micro.cucumber.stepdefs.integration", "com.example.micro.cucumber.stepdefs.common"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/integration-tests.html",
                "json:target/cucumber-reports/integration-tests.json"
        },
        tags = "@integration"
)
public class IntegrationTestRunner {
        // Integration test runner
}