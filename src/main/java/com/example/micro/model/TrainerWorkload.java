package com.example.micro.model;

import com.example.micro.model.YearSummary;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TrainerWorkload represents a trainer's monthly workout summary
 */
@Entity
public class TrainerWorkload {

    @Id
    private String username;

    private String firstName;
    private String lastName;
    private boolean isActive;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<YearSummary> years = new ArrayList<>();


    public TrainerWorkload() {
    }

    // All-args constructor
    public TrainerWorkload(String username, String firstName, String lastName, boolean isActive, List<YearSummary> years) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.years = years;
    }

    // Constructor without years list
    public TrainerWorkload(String username, String firstName, String lastName, boolean isActive) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.years = new ArrayList<>();
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

    // Custom Getter and Setter for 'years'
    public List<YearSummary> getYears() {
        return years;
    }

    public void setYears(List<YearSummary> years) {
        this.years = years;
    }

    // Custom method to get or create a YearSummary for a specific year
    public YearSummary getOrCreateYear(int year) {
        for (YearSummary yearSummary : years) {
            if (yearSummary.getYear() == year) {
                return yearSummary;
            }
        }

        YearSummary newYear = new YearSummary();
        newYear.setYear(year);
        newYear.setTrainer(this);
        years.add(newYear);
        return newYear;
    }
}
