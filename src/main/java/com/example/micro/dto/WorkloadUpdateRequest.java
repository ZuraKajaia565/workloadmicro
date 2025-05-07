package com.example.micro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request object for updating trainer workload
 */
public class WorkloadUpdateRequest {

    @NotBlank(message = "Trainer username is required")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private boolean isActive;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @Positive(message = "Training duration must be positive")
    private int trainingDuration;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    public WorkloadUpdateRequest() {
    }

    // All-args constructor
    public WorkloadUpdateRequest(String username, String firstName, String lastName,
                                 boolean isActive, LocalDate trainingDate,
                                 int trainingDuration, ActionType actionType) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.actionType = actionType;
    }

    // Custom Getter and Setter for 'username'
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Custom Getter and Setter for 'firstName'
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Custom Getter and Setter for 'lastName'
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Custom Getter and Setter for 'isActive'
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    // Custom Getter and Setter for 'trainingDate'
    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    // Custom Getter and Setter for 'trainingDuration'
    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    // Custom Getter and Setter for 'actionType'
    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    // Enum for ActionType
    public enum ActionType {
        ADD, DELETE
    }
}
