package com.github.phoswald.fitbit.viewer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "fitbit_activity_level_")
@IdClass(ActivityLevelEntity.ActivityLevelId.class)
public class ActivityLevelEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "log_id_", nullable = false)
    private long logId;

    @Id
    @Column(name = "level_name_", length = 32, nullable = false)
    private String levelName;

    @Column(name = "level_minutes_")
    private Integer levelMinutes;

    @Column(name = "sort_index_")
    private Integer sortIndex;

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

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Integer getLevelMinutes() {
        return levelMinutes;
    }

    public void setLevelMinutes(Integer levelMinutes) {
        this.levelMinutes = levelMinutes;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    record ActivityLevelId(String userId, Long logId, String levelName) implements java.io.Serializable { }
}
