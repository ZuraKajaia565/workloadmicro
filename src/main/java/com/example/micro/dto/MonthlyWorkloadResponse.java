package com.example.micro.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response object for monthly workload
 */
@NoArgsConstructor
@Getter
@Setter
public class MonthlyWorkloadResponse {

    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private int year;
    private int month;
    private int summaryDuration;
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
}
