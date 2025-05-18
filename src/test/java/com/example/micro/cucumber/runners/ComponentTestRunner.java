package com.example.micro.cucumber.runners;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/component",
        glue = {"com.example.micro.cucumber.stepdefs"},
        tags = "@component",
        plugin = {"pretty", "html:target/cucumber-reports/component"}
)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")  // Add this line to activate the test profile
public class ComponentTestRunner {
    // No changes needed to the class body
}