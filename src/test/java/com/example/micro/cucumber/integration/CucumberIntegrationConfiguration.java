// src/test/java/com/example/micro/cucumber/integration/CucumberIntegrationConfiguration.java
package com.example.micro.cucumber.integration;

import com.example.micro.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@ActiveProfiles("test")
public class CucumberIntegrationConfiguration {
    // Integration test configuration
}