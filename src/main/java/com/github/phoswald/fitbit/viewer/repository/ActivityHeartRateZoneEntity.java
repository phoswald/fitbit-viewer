package com.github.phoswald.fitbit.viewer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;

@Entity
@Table(name = "fitbit_activity_heartrate_zone_")
@IdClass(ActivityHeartRateZoneEntity.ActivityHeartRateZoneId.class)
public class ActivityHeartRateZoneEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "log_id_", nullable = false)
    private long logId;

    @Id
    @Column(name = "zone_name_", length = 32, nullable = false)
    private String zoneName;

    @Column(name = "zone_minutes_")
    private Integer zoneMinutes;

    @Column(name = "calories_")
    private Double calories;

    @Column(name = "heartrate_min_")
    private Integer heartRateMin;

    @Column(name = "heartrate_max_")
    private Integer heartRateMax;

    public static ActivityHeartRateZoneEntity create(String userId, long logId, ActivityApiClient.HeartRateZone entry) {
        ActivityHeartRateZoneEntity entity = new ActivityHeartRateZoneEntity();
        entity.setUserId(userId);
        entity.setLogId(logId);
        entity.setZoneName(entry.name());
        entity.setZoneMinutes(entry.minutes());
        entity.setCalories(entry.caloriesOut());
        entity.setHeartRateMin(entry.min());
        entity.setHeartRateMax(entry.max());
        return entity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Integer getZoneMinutes() {
        return zoneMinutes;
    }

    public void setZoneMinutes(Integer zoneMinutes) {
        this.zoneMinutes = zoneMinutes;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Integer getHeartRateMin() {
        return heartRateMin;
    }

    public void setHeartRateMin(Integer heartRateMin) {
        this.heartRateMin = heartRateMin;
    }

    public Integer getHeartRateMax() {
        return heartRateMax;
    }

    public void setHeartRateMax(Integer heartRateMax) {
        this.heartRateMax = heartRateMax;
    }

    record ActivityHeartRateZoneId(String userId, Long logId, String zoneName) implements java.io.Serializable { }
}
