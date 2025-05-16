package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.micro.component.stepdefs"},
        plugin = {"pretty", "html:target/cucumber-reports"}
)
public class CucumberRunnerTest {
    // Empty class body
}