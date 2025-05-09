package com.example.micro.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TrainerWorkload represents a trainer's workout summary
 */
@Entity
public class TrainerWorkload {

    @Id
    private String username;

    private String firstName;
    private String lastName;
    private boolean isActive;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "trainer_username")
    private List<YearSummary> years = new ArrayList<>();

    // No-args constructor
    public TrainerWorkload() {
    }

    // All-args constructor
    public TrainerWorkload(String username, String firstName, String lastName, boolean isActive, List<YearSummary> years) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.years = years != null ? years : new ArrayList<>();
    }

    // Constructor without years list
    public TrainerWorkload(String username, String firstName, String lastName, boolean isActive) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.years = new ArrayList<>();
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
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public List<YearSummary> getYears() {
        return years;
    }

    public void setYears(List<YearSummary> years) {
        this.years = years != null ? years : new ArrayList<>();
    }
}