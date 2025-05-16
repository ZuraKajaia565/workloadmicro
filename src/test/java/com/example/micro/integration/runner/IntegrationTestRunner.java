package com.example.micro.integration.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/integration",
        glue = {"com.zura.workload.integration.stepdefs"}, // Corrected package path
        plugin = {"pretty", "html:target/cucumber-reports/integration"}
)
public class IntegrationTestRunner {
}