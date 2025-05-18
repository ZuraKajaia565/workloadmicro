// src/test/java/com/example/micro/cucumber/component/CucumberComponentConfiguration.java
package com.example.micro.cucumber.component;

import com.example.micro.MicroApplication;
import com.example.micro.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        properties = {
                "spring.main.allow-bean-definition-overriding=true"
        }
)
@Import(TestConfig.class)
@ActiveProfiles("test")
public class CucumberComponentConfiguration {
    // Component test configuration
}