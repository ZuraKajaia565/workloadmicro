package com.example.micro.dto;

import java.time.LocalDate;

/**
 * Represents a request to create or update a trainer's workload.
 */
public class WorkloadRequest {

    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private LocalDate trainingDate;
    private int trainingDuration;

    public WorkloadRequest() {
        // Default constructor
    }

    public WorkloadRequest(String username, String firstName, String lastName, boolean active,
                           LocalDate trainingDate, int trainingDuration) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    // Getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    // Optional: validation methods

    public boolean isValid() {
        return username != null && !username.trim().isEmpty()
                && firstName != null && !firstName.trim().isEmpty()
                && lastName != null && !lastName.trim().isEmpty()
                && trainingDate != null
                && trainingDuration > 0;
    }
}
