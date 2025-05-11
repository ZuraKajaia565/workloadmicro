package com.example.micro.messaging;

import java.io.Serializable;

public class WorkloadMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private int year;
    private int month;
    private int trainingDuration;
    private MessageType messageType;
    private String transactionId;

    public enum MessageType {
        CREATE_UPDATE, DELETE
    }

    // Default constructor for serialization
    public WorkloadMessage() {
    }

    public WorkloadMessage(String username, String firstName, String lastName,
                           boolean isActive, int year, int month,
                           int trainingDuration, MessageType messageType,
                           String transactionId) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.year = year;
        this.month = month;
        this.trainingDuration = trainingDuration;
        this.messageType = messageType;
        this.transactionId = transactionId;
    }

    // Getters and setters
    // [All standard getters and setters would go here]

    @Override
    public String toString() {
        return "WorkloadMessage{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isActive=" + isActive +
                ", year=" + year +
                ", month=" + month +
                ", trainingDuration=" + trainingDuration +
                ", messageType=" + messageType +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}