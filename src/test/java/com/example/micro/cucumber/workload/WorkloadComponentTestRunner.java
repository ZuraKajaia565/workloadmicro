package com.example.micro.cucumber.workload;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/workload",
    glue = "com.example.micro.cucumber.workload",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/workload-component-report.html",
        "json:target/cucumber-reports/workload-component-report.json"
    },
    monochrome = true
)
public class WorkloadComponentTestRunner {
} 