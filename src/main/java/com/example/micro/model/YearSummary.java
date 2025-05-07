package com.example.micro.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * YearSummary represents a year of workout summaries
 */
@Entity
public class YearSummary {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(name = "\"year\"") // Escape the column name with double quotes
  private int year;

  @ManyToOne
  @JoinColumn(name = "trainer_username")
  private TrainerWorkload trainer;

  @OneToMany(mappedBy = "yearSummary", cascade = CascadeType.ALL,
             orphanRemoval = true)
  private List<MonthSummary> months = new ArrayList<>();

  // No-args constructor
  public YearSummary() {
  }

  // All-args constructor
  public YearSummary(Long id, int year, TrainerWorkload trainer, List<MonthSummary> months) {
    this.id = id;
    this.year = year;
    this.trainer = trainer;
    this.months = months;
  }

  // Custom Getter and Setter for 'id'
  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  // Custom Getter and Setter for 'year'
  public int getYear() { return year; }

  public void setYear(int year) { this.year = year; }

  // Custom Getter and Setter for 'trainer'
  public TrainerWorkload getTrainer() { return trainer; }

  public void setTrainer(TrainerWorkload trainer) { this.trainer = trainer; }

  // Custom Getter and Setter for 'months'
  public List<MonthSummary> getMonths() { return months; }

  public void setMonths(List<MonthSummary> months) { this.months = months; }

  // Custom method to get or create a MonthSummary for a specific month
  public MonthSummary getOrCreateMonth(int month) {
    for (MonthSummary monthSummary : months) {
      if (monthSummary.getMonth() == month) {
        return monthSummary;
      }
    }

    MonthSummary newMonth = new MonthSummary();
    newMonth.setMonth(month);
    newMonth.setYearSummary(this);
    newMonth.setSummaryDuration(0);
    months.add(newMonth);
    return newMonth;
  }
}
