package com.example.micro.cucumber.integration;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/integration",
    glue = "com.example.micro.cucumber.integration",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/integration-report.html",
        "json:target/cucumber-reports/integration-report.json"
    },
    monochrome = true
)
public class MicroservicesIntegrationTestRunner {
} 