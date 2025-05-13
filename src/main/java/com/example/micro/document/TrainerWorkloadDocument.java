package com.example.micro.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@CompoundIndexes({
        @CompoundIndex(name = "fullname_idx", def = "{'firstName': 1, 'lastName': 1}")
})
public class TrainerWorkloadDocument {

    @Id
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private List<YearSummary> years = new ArrayList<>();

    // Nested document for Year Summary
    public static class YearSummary {
        private int year;
        private List<MonthSummary> months = new ArrayList<>();

        // Getters and setters
        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public List<MonthSummary> getMonths() {
            return months;
        }

        public void setMonths(List<MonthSummary> months) {
            this.months = months != null ? months : new ArrayList<>();
        }
    }

    // Nested document for Month Summary
    public static class MonthSummary {
        private int month;
        private int trainingsSummaryDuration;

        // Getters and setters
        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getTrainingsSummaryDuration() {
            return trainingsSummaryDuration;
        }

        public void setTrainingsSummaryDuration(int trainingsSummaryDuration) {
            this.trainingsSummaryDuration = trainingsSummaryDuration;
        }
    }

    // Constructors, getters and setters
    public TrainerWorkloadDocument() {
    }

    public TrainerWorkloadDocument(String username, String firstName, String lastName, boolean isActive) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
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

    public List<YearSummary> getYears() {
        return years;
    }

    public void setYears(List<YearSummary> years) {
        this.years = years != null ? years : new ArrayList<>();
    }
}