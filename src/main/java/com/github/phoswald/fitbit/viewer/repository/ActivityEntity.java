package com.github.phoswald.fitbit.viewer.repository;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;

@Entity
@Table(name = "fitbit_activity_")
@IdClass(ActivityEntity.ActivityId.class)
public class ActivityEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "log_id_", nullable = false)
    private long logId;

    @Column(name = "log_type_", length = 32)
    private String logType;

    @Column(name = "activity_type_id_")
    private Integer activityTypeId;

    @Column(name = "activity_name_", length = 64)
    private String activityName;

    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "beg_date_time_")
    private OffsetDateTime begDateTime;

    @Column(name = "end_date_time_")
    private OffsetDateTime endDateTime;

    @Column(name = "duration_minutes_")
    private Integer durationMinutes;

    @Column(name = "active_duration_minutes_")
    private Integer activeDurationMinutes;

    @Column(name = "distance_")
    private Double distance;

    @Column(name = "distance_unit_", length = 16)
    private String distanceUnit;

    @Column(name = "steps_")
    private Integer steps;

    @Column(name = "calories_")
    private Integer calories;

    @Column(name = "speed_")
    private Double speed;

    @Column(name = "pace_")
    private Double pace;

    @Column(name = "average_heart_rate_")
    private Integer averageHeartRate;

    @Column(name = "active_zone_minutes_")
    private Integer activeZoneMinutes;

    @Column(name = "source_type_")
    private String sourceType;

    @Column(name = "source_name_")
    private String sourceName;

    @Column(name = "source_url_")
    private String sourceUrl;

    @Column(name = "source_features_")
    private String sourceFeatures;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "user_id_", referencedColumnName = "user_id_"),
        @JoinColumn(name = "log_id_", referencedColumnName = "log_id_")
    })
    @OrderBy("sortIndex ASC")
    private List<ActivityLevelEntity> activityLevels = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "user_id_", referencedColumnName = "user_id_"),
        @JoinColumn(name = "log_id_", referencedColumnName = "log_id_")
    })
    @OrderBy("heartRateMin ASC")
    private List<ActivityHeartRateZoneEntity> heartRateZones = new ArrayList<>();

    public static ActivityEntity create(String userId, ActivityApiClient.ActivityEntry entry) {
        ActivityEntity entity = new ActivityEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setLogId(requireNonNull(entry.logId(), "logId"));
        entity.setLogType(entry.logType());
        entity.setActivityTypeId(entry.activityTypeId());
        entity.setActivityName(entry.activityName());
        entity.setDate(OffsetDateTime.parse(requireNonNull(entry.startTime(), "startTime")).toLocalDate());
        entity.setBegDateTime(entry.startTime() == null ? null : OffsetDateTime.parse(entry.startTime()));
        entity.setEndDateTime(entry.startTime() == null || entry.duration() == null ? null : OffsetDateTime.parse(entry.startTime()).plusSeconds(entry.duration() / 1000));
        entity.setDurationMinutes(entry.duration() == null ? null : (int) (entry.duration() / 60_000));
        entity.setActiveDurationMinutes(entry.activeDuration() == null ? null : (int) (entry.activeDuration() / 60_000));
        entity.setDistance(entry.distance());
        entity.setDistanceUnit(entry.distanceUnit());
        entity.setSteps(entry.steps());
        entity.setCalories(entry.calories());
        entity.setSpeed(entry.speed());
        entity.setPace(entry.pace());
        // entry.elevationGain(); // immer 0
        // entry.floors(); // nicht vorhanden
        entity.setAverageHeartRate(entry.averageHeartRate());
        entity.setActiveZoneMinutes(entry.activeZoneMinutes().totalMinutes()); // redundant: sum over minutes * minuteMultiplier
        if(entry.source() != null) {
            entity.setSourceType(entry.source().type());
            entity.setSourceName(entry.source().name());
            entity.setSourceUrl(entry.source().url());
            entity.setSourceFeatures(entry.source().trackerFeatures() == null ? null : entry.source().trackerFeatures().toString());
        }
        if(entry.activityLevel() != null && !entry.activityLevel().isEmpty()) {
            int nextSortIndex = 1;
            for(var activityLevel : entry.activityLevel()) {
                entity.getActivityLevels().add(ActivityLevelEntity.create(
                        userId, entity.getLogId(), nextSortIndex++, activityLevel));
            }
        }
        if(entry.heartRateZones() != null && !entry.heartRateZones().isEmpty()) {
            for(var heartRateZone : entry.heartRateZones()) {
                entity.getHeartRateZones().add(ActivityHeartRateZoneEntity.create(
                        userId, entity.getLogId(), heartRateZone));
            }
        }
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

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public Integer getActivityTypeId() {
        return activityTypeId;
    }

    public void setActivityTypeId(Integer activityTypeId) {
        this.activityTypeId = activityTypeId;
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

    public Integer getActiveDurationMinutes() {
        return activeDurationMinutes;
    }

    public void setActiveDurationMinutes(Integer activeDurationMinutes) {
        this.activeDurationMinutes = activeDurationMinutes;
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

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getPace() {
        return pace;
    }

    public void setPace(Double pace) {
        this.pace = pace;
    }

    public Integer getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(Integer averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    public Integer getActiveZoneMinutes() {
        return activeZoneMinutes;
    }

    public void setActiveZoneMinutes(Integer activeZoneMinutes) {
        this.activeZoneMinutes = activeZoneMinutes;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceFeatures() {
        return sourceFeatures;
    }

    public void setSourceFeatures(String sourceFeatures) {
        this.sourceFeatures = sourceFeatures;
    }

    public List<ActivityLevelEntity> getActivityLevels() {
        return activityLevels;
    }

    public void setActivityLevels(List<ActivityLevelEntity> activityLevels) {
        this.activityLevels = activityLevels;
    }

    public List<ActivityHeartRateZoneEntity> getHeartRateZones() {
        return heartRateZones;
    }

    public void setHeartRateZones(List<ActivityHeartRateZoneEntity> heartRateZones) {
        this.heartRateZones = heartRateZones;
    }

    record ActivityId(String userId, Long logId) implements java.io.Serializable { }
}
