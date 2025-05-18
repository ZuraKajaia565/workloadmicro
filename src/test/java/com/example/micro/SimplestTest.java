package com.example.micro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class SimplestTest {

    @Test
    public void contextLoads() {
        // Will pass if the context loads successfully
        assertTrue(true);
    }
}