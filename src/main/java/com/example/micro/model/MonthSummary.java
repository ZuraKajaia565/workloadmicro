package com.example.micro.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * MonthSummary represents a month of workout summaries.
 */
@Entity
@ToString
@EqualsAndHashCode
public class MonthSummary {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(name = "\"month\"") // Escape the column name with double quotes
  private int month;

  @Column(name = "summary_duration") private int summaryDuration; // in minutes

  @ManyToOne @JoinColumn(name = "year_id") private YearSummary yearSummary;

  public MonthSummary() {
  }

  // All-args constructor
  public MonthSummary(Long id, int month, int summaryDuration, YearSummary yearSummary) {
    this.id = id;
    this.month = month;
    this.summaryDuration = summaryDuration;
    this.yearSummary = yearSummary;
  }

  // Custom Getter and Setter for 'id'
  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  // Custom Getter and Setter for 'month'
  public int getMonth() { return month; }

  public void setMonth(int month) { this.month = month; }

  // Custom Getter and Setter for 'summaryDuration'
  public int getSummaryDuration() { return summaryDuration; }

  public void setSummaryDuration(int summaryDuration) {
    this.summaryDuration = summaryDuration;
  }

  // Custom Getter and Setter for 'yearSummary'
  public YearSummary getYearSummary() { return yearSummary; }

  public void setYearSummary(YearSummary yearSummary) {
    this.yearSummary = yearSummary;
  }
}
