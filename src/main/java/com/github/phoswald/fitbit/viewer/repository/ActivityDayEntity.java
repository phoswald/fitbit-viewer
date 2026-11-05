package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "fitbit_activity_day_")
@NamedQuery(
        name = "ActivityDayEntity.loadByUserIdAndDateRange",
        query = "SELECT a FROM ActivityDayEntity a WHERE a.userId = :userId AND a.date >= :dateBeg AND a.date <= :dateEnd ORDER BY a.date"
)
@IdClass(ActivityDayEntity.ActivityDayId.class)
public class ActivityDayEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

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

    public record ActivityDayId(String userId, LocalDate date) implements java.io.Serializable { }
}
