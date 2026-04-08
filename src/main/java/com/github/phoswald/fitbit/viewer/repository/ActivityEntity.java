package com.github.phoswald.fitbit.viewer.repository;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;

@Entity
@Table(name = "fitbit_activity_")
@NamedQuery(
        name = "ActivityEntity.loadByUserIdAndLogId",
        query = "SELECT a FROM ActivityEntity a WHERE a.userId = :userId AND a.logId = :logId"
)
@IdClass(ActivityEntity.ActivityId.class)
public class ActivityEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Id
    @Column(name = "log_id_", nullable = false)
    private Long logId;

    @Column(name = "log_type_", length = 32)
    private String logType;

    @Column(name = "activity_name_", length = 64)
    private String activityName;

    @Column(name = "beg_date_time_")
    private OffsetDateTime begDateTime;

    @Column(name = "end_date_time_")
    private OffsetDateTime endDateTime;

    @Column(name = "duration_minutes_")
    private Integer durationMinutes;

    @Column(name = "calories_")
    private Integer calories;

    @Column(name = "steps_")
    private Integer steps;

    @Column(name = "distance_")
    private Double distance;

    @Column(name = "distance_unit_", length = 16)
    private String distanceUnit;

    @Column(name = "average_heart_rate_")
    private Integer averageHeartRate;

    public static ActivityEntity create(String userId, ActivityApiClient.ActivityEntry entry) {
        ActivityEntity entity = new ActivityEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(OffsetDateTime.parse(requireNonNull(entry.startTime(), "startTime")).toLocalDate());
        entity.setLogId(requireNonNull(entry.logId(), "logId"));
        entity.setLogType(entry.logType());
        entity.setActivityName(entry.activityName());
        entity.setBegDateTime(entry.startTime() == null ? null : OffsetDateTime.parse(entry.startTime()));
        entity.setEndDateTime(entry.startTime() == null || entry.duration() == null ? null : OffsetDateTime.parse(entry.startTime()).plusSeconds(entry.duration() / 1000));
        entity.setDurationMinutes(entry.duration() == null ? null : (int) (entry.duration() / 60_000));
        entity.setCalories(entry.calories());
        entity.setSteps(entry.steps());
        entity.setDistance(entry.distance());
        entity.setDistanceUnit(entry.distanceUnit());
        entity.setAverageHeartRate(entry.averageHeartRate());
        return entity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public OffsetDateTime getBegDateTime() {
        return begDateTime;
    }

    public void setBegDateTime(OffsetDateTime begDateTime) {
        this.begDateTime = begDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public Integer getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(Integer averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    record ActivityId(String userId, LocalDate date, Long logId) implements java.io.Serializable { }
}
