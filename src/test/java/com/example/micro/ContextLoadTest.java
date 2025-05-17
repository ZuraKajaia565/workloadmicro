package com.example.micro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class ContextLoadTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        // This will pass if the Spring context loads correctly
        assertNotNull(context);
    }
}