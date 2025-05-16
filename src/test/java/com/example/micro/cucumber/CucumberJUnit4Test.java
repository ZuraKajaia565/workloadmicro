package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.micro.component.stepdefs", "com.example.micro.integration.stepdefs"},
        plugin = {"pretty", "html:target/cucumber-reports"}
)
public class CucumberJUnit4Test {
    // Using JUnit 4 runner for Cucumber
}