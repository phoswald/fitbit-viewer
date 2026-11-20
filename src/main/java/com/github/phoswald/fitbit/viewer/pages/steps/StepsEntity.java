package com.github.phoswald.fitbit.viewer.pages.steps;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateOf;
import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.stepCountOf;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInner;

@Entity
@Table(name = "fitbit_steps_")
@NamedQuery(
        name = "StepsEntity.loadByUserIdAndDateRange",
        query = "SELECT s FROM StepsEntity s WHERE s.userId = :userId AND s.date >= :dateBeg AND s.date <= :dateEnd ORDER BY s.date"
)
@IdClass(StepsEntity.StepsId.class)
public class StepsEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "step_count_")
    private Integer stepCount;

    public static StepsEntity create(String userId, DailyRollUpDataPointsResponseRollupDataPointsInner dataPoint) {
        StepsEntity entity = new StepsEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(requireNonNull(dateOf(dataPoint), "date"));
        entity.setStepCount(stepCountOf(dataPoint));
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

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public record StepsId(String userId, LocalDate date) implements java.io.Serializable { }
}
