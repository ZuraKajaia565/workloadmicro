package com.example.micro.cucumber.utils;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Helper methods for integration tests
 */
public class TestUtils {

    /**
     * Creates a RestTemplate with a timeout
     */
    public static RestTemplate createRestTemplateWithTimeout(int timeoutSeconds) {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(timeoutSeconds))
                .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Waits for a condition to be true with a timeout
     */
    public static boolean waitForCondition(Condition condition, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition.isMet()) {
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Interface for a condition to wait for
     */
    @FunctionalInterface
    public interface Condition {
        boolean isMet();
    }

    /**
     * Creates a test training session for use in tests
     */
    public static Map<String, Object> createTestTrainingSession() {
        Map<String, Object> session = new HashMap<>();
        session.put("trainerId", "testTrainer");
        session.put("clientId", "testClient");
        session.put("date", "2025-10-15");
        session.put("startTime", "14:00");
        session.put("duration", 60);
        session.put("notes", "Test session");
        return session;
    }
}