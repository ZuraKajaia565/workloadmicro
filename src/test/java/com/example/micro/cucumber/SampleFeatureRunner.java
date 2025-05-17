package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner specifically for the sample.feature file
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/sample.feature",
                 // Only include the specific stepdefs for this feature
                 glue = {"com.example.micro.cucumber.stepdefs"},
                 plugin = {"pretty"})
public class SampleFeatureRunner {
  // Empty class body
}
