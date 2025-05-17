// src/test/java/com/example/micro/TestConfig.java
package com.example.micro;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import com.example.micro.repository.TrainerWorkloadRepository;
import org.mockito.Mockito;

@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class
})
public class TestConfig {

    @Bean
    @Primary
    public TrainerWorkloadRepository trainerWorkloadRepository() {
        return Mockito.mock(TrainerWorkloadRepository.class);
    }

    // Add other mock beans for testing here
}