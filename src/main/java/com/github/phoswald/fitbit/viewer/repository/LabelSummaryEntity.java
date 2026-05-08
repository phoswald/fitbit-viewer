package com.github.phoswald.fitbit.viewer.repository;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "fitbit_label_summary_")
@NamedQuery(
        name = "LabelSummaryEntity.loadByUserId",
        query = "SELECT s FROM LabelSummaryEntity s WHERE s.userId = :userId ORDER BY s.label"
)
@IdClass(LabelSummaryEntity.LabelSummaryId.class)
public class LabelSummaryEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "label_", length = 64, nullable = false)
    private String label;

    @Column(name = "count_")
    private Long count;

    @Column(name = "date_min_")
    private LocalDate dateMin;

    @Column(name = "date_max_")
    private LocalDate dateMax;

    @Column(name = "duration_minutes_min_")
    private Double durationMinutesMin;

    @Column(name = "duration_minutes_max_")
    private Double durationMinutesMax;

    @Column(name = "calories_min_")
    private Integer caloriesMin;

    @Column(name = "calories_max_")
    private Integer caloriesMax;

    @Column(name = "steps_min_")
    private Integer stepsMin;

    @Column(name = "steps_max_")
    private Integer stepsMax;

    @Column(name = "distance_min_")
    private Double distanceMin;

    @Column(name = "distance_max_")
    private Double distanceMax;

    @Column(name = "heart_rate_min_")
    private Integer heartRateMin;

    @Column(name = "heart_rate_max_")
    private Integer heartRateMax;

    public String getUserId() { return userId; }
    public String getLabel() { return label; }
    public Long getCount() { return count; }
    public LocalDate getDateMin() { return dateMin; }
    public LocalDate getDateMax() { return dateMax; }
    public Double getDurationMinutesMin() { return durationMinutesMin; }
    public Double getDurationMinutesMax() { return durationMinutesMax; }
    public Integer getCaloriesMin() { return caloriesMin; }
    public Integer getCaloriesMax() { return caloriesMax; }
    public Integer getStepsMin() { return stepsMin; }
    public Integer getStepsMax() { return stepsMax; }
    public Double getDistanceMin() { return distanceMin; }
    public Double getDistanceMax() { return distanceMax; }
    public Integer getHeartRateMin() { return heartRateMin; }
    public Integer getHeartRateMax() { return heartRateMax; }

    record LabelSummaryId(String userId, String label) implements Serializable { }
}
