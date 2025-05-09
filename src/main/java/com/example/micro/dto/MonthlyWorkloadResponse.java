package com.example.micro.dto;

/**
 * Response object for monthly workload
 */
public class MonthlyWorkloadResponse {

    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private int year;
    private int month;
    private int summaryDuration;

    public MonthlyWorkloadResponse() {
    }

    public MonthlyWorkloadResponse(String username, String firstName, String lastName,
                                   boolean active, int year, int month, int duration) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = active;
        this.year = year;
        this.month = month;
        this.summaryDuration = duration;
    }

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
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getSummaryDuration() {
        return summaryDuration;
    }

    public void setSummaryDuration(int summaryDuration) {
        this.summaryDuration = summaryDuration;
    }
}