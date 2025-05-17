package com.example.micro.cucumber;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Runner specifically for the sample.feature file
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/sample.feature",
        glue = {"com.example.micro.cucumber.stepdefs", "com.example.micro.cucumber"},
        plugin = {"pretty"}
)
public class SampleFeatureRunner {
    // Empty class body
}