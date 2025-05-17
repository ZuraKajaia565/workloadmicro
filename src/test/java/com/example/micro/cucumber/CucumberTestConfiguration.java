// Create this new file in: src/test/java/com/example/micro/cucumber/CucumberTestConfiguration.java
package com.example.micro.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import io.cucumber.spring.CucumberContextConfiguration;
import com.example.micro.MicroApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = MicroApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberTestConfiguration {
        // This class serves as a bridge between Cucumber and Spring Boot
        // It's required for proper context loading in Cucumber tests
}