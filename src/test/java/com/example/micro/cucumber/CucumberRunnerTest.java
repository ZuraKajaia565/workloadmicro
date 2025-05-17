// Replace src/test/java/com/example/micro/cucumber/CucumberRunnerTest.java
package com.example.micro.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        // Define features location
        features = "src/test/resources/features",
        // Specify the correct package for step definitions and include the configuration class
        glue = {"com.example.micro.cucumber.stepdefs", "com.example.micro.cucumber"},
        // Add plugins for reporting
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json"
        }
)
public class CucumberRunnerTest {
    // Empty class body
}