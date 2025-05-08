package com.example.micro.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * YearSummary represents a year of workout summaries
 */
@Entity
public class YearSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "\"year\"")
  private int year;

  @Column(name = "trainer_username")
  private String trainerUsername;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "year_id")
  private List<MonthSummary> months = new ArrayList<>();

  // No-args constructor
  public YearSummary() {
  }

  // All-args constructor
  public YearSummary(Long id, int year, String trainerUsername, List<MonthSummary> months) {
    this.id = id;
    this.year = year;
    this.trainerUsername = trainerUsername;
    this.months = months != null ? months : new ArrayList<>();
  }

  // Getters and setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public String getTrainerUsername() {
    return trainerUsername;
  }

  public void setTrainerUsername(String trainerUsername) {
    this.trainerUsername = trainerUsername;
  }

  public List<MonthSummary> getMonths() {
    return months;
  }

  public void setMonths(List<MonthSummary> months) {
    this.months = months != null ? months : new ArrayList<>();
  }

  /**
   * Gets or creates a MonthSummary for a specific month
   */
  public MonthSummary getOrCreateMonth(int month) {
    for (MonthSummary monthSummary : months) {
      if (monthSummary.getMonth() == month) {
        return monthSummary;
      }
    }

    MonthSummary newMonth = new MonthSummary();
    newMonth.setMonth(month);
    newMonth.setYearId(this.id);
    newMonth.setSummaryDuration(0);
    months.add(newMonth);
    return newMonth;
  }
}