package com.example.micro.model;

import jakarta.persistence.*;

/**
 * MonthSummary represents a month of workout summaries.
 */
@Entity
public class MonthSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "\"month\"")
  private int month;

  @Column(name = "summary_duration")
  private int summaryDuration; // in minutes

  @Column(name = "year_id")
  private Long yearId;

  // ManyToOne relationship with YearSummary (but not direct property to avoid circular reference)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "year_id", insertable = false, updatable = false)
  private YearSummary yearSummary;

  // No-args constructor
  public MonthSummary() {
  }

  // All-args constructor
  public MonthSummary(Long id, int month, int summaryDuration, Long yearId) {
    this.id = id;
    this.month = month;
    this.summaryDuration = summaryDuration;
    this.yearId = yearId;
  }

  // Getters and setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public Long getYearId() {
    return yearId;
  }

  public void setYearId(Long yearId) {
    this.yearId = yearId;
  }

  public YearSummary getYearSummary() {
    return yearSummary;
  }

  // This is needed for proper DTO conversion but not for JPA
  public void setYearSummary(YearSummary yearSummary) {
    this.yearSummary = yearSummary;
  }
}