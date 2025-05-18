// src/test/java/com/example/micro/config/TestConfig.java
package com.example.micro.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.example.micro.repository.TrainerWorkloadRepository;

@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class TestConfig {

    @Bean
    @Primary
    public TrainerWorkloadRepository trainerWorkloadRepository() {
        return Mockito.mock(TrainerWorkloadRepository.class);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return Mockito.mock(MongoTemplate.class);
    }
}