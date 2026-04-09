package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseDate;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.HeartRateApiClient;

@Entity
@Table(name = "fitbit_heartrate_")
@NamedQuery(
        name = "HeartRateEntity.loadByUserIdAndDateRange",
        query = "SELECT h FROM HeartRateEntity h WHERE h.userId = :userId AND h.date >= :begDate AND h.date <= :endDate ORDER BY h.date"
)
@IdClass(HeartRateEntity.HeartRateId.class)
public class HeartRateEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "resting_heart_rate_")
    private Integer restingHeartRate;

    public static HeartRateEntity create(String userId, HeartRateApiClient.HeartRateEntry entry) {
        HeartRateEntity entity = new HeartRateEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(parseDate(requireNonNull(entry.dateTime(), "date")));
        entity.setRestingHeartRate(entry.value() == null ? null : entry.value().restingHeartRate());
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

    public Integer getRestingHeartRate() {
        return restingHeartRate;
    }

    public void setRestingHeartRate(Integer restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }

    public record HeartRateId(String userId, LocalDate date) implements java.io.Serializable { }
}
