package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/sample.feature",
        // This is the key change - specify ONLY the specific package with step definitions
        glue = {"com.example.micro.cucumber.stepdefs"},
        plugin = {"pretty"}
)
public class SampleFeatureRunner {
    // Empty class body
}
