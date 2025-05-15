// src/test/java/com/example/micro/component/runners/ComponentTestRunner.java
package com.example.micro.component.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features/component",
        glue = {"com.example.micro.component.stepdefs"},
        plugin = {"pretty", "html:target/cucumber-reports/component"}
)
public class ComponentTestRunner {
}