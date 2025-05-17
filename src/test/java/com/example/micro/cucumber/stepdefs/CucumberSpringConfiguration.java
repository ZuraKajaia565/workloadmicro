package com.example.micro.cucumber.stepdefs;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = com.example.micro.MicroApplication.class)
public class CucumberSpringConfiguration {
    // This class is only used to configure Spring for Cucumber
}
