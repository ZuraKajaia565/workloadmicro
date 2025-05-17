package com.example.micro;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * Test configuration that disables real MongoDB repositories and provides mocks
 */
@TestConfiguration
@Profile("test")
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class
})
public class TestConfig {

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return Mockito.mock(MongoTemplate.class);
    }

    // Remove the trainerWorkloadRepository bean completely
    // We'll let Spring use either the real repository or create a mock as needed
}