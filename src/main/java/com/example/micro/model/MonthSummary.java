package com.example.micro.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * MonthSummary represents a month of workout summaries.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MonthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int month;

    private int summaryDuration; // in minutes

    @ManyToOne
    @JoinColumn(name = "year_id")
    private YearSummary yearSummary;

    // Custom Getter and Setter for 'id'
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Custom Getter and Setter for 'month'
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    // Custom Getter and Setter for 'summaryDuration'
    public int getSummaryDuration() {
        return summaryDuration;
    }

    public void setSummaryDuration(int summaryDuration) {
        this.summaryDuration = summaryDuration;
    }

    // Custom Getter and Setter for 'yearSummary'
    public YearSummary getYearSummary() {
        return yearSummary;
    }

    public void setYearSummary(YearSummary yearSummary) {
        this.yearSummary = yearSummary;
    }

}
