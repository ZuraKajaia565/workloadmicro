package com.example.micro.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import io.cucumber.spring.CucumberContextConfiguration;
import com.example.micro.config.MongoTestConfig;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
@Import(MongoTestConfig.class)  // Import our MongoDB test config
public class CucumberTestConfiguration {
    // No changes needed here
}