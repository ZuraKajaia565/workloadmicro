package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/integration",
        glue = {"com.example.micro.cucumber.stepdefs.integration", "com.example.micro.cucumber"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/integration.html",
                "json:target/cucumber-reports/integration.json"
        },
        tags = "not @skip"
)
public class IntegrationTestRunner {
        // Empty class - just a runner
}