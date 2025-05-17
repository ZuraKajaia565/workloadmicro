package com.example.micro.cucumber.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.micro.cucumber.stepdefs"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/component-tests.html",
                "json:target/cucumber-reports/component-tests.json"
        },
        tags = "@component"  // Only run scenarios tagged with @component
)
public class ComponentTestRunner {
    // This class serves as a test runner
}