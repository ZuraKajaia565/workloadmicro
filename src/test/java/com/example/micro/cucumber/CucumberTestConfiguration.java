package com.example.micro.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuration class for Cucumber tests without
 * the @CucumberContextConfiguration annotation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberTestConfiguration {
  // Empty class body - this class can be safely deleted or kept as a regular
  // configuration class
}
