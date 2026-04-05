package com.github.phoswald.fitbit.viewer.repository;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.AzmApiClient;

@Entity
@Table(name = "fitbit_azm_")
@NamedQuery(
        name = "AzmEntity.loadByUserIdAndDateRange",
        query = "SELECT a FROM AzmEntity a WHERE a.userId = :userId AND a.date >= :begDate AND a.date <= :endDate ORDER BY a.date"
)
@IdClass(AzmEntity.AzmId.class)
public class AzmEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "active_zone_minutes_")
    private Integer activeZoneMinutes;

    @Column(name = "fat_burn_active_zone_minutes_")
    private Integer fatBurnActiveZoneMinutes;

    @Column(name = "cardio_active_zone_minutes_")
    private Integer cardioActiveZoneMinutes;

    @Column(name = "peak_active_zone_minutes_")
    private Integer peakActiveZoneMinutes;

    public static AzmEntity create(String userId, AzmApiClient.AzmEntry entry) {
        AzmApiClient.AzmValue value = entry.value();
        AzmEntity entity = new AzmEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(LocalDate.parse(requireNonNull(entry.dateTime(), "date")));
        entity.setActiveZoneMinutes(value == null ? null : value.activeZoneMinutes());
        entity.setFatBurnActiveZoneMinutes(value == null ? null : value.fatBurnActiveZoneMinutes());
        entity.setCardioActiveZoneMinutes(value == null ? null : value.cardioActiveZoneMinutes());
        entity.setPeakActiveZoneMinutes(value == null ? null : value.peakActiveZoneMinutes());
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

    public Integer getActiveZoneMinutes() {
        return activeZoneMinutes;
    }

    public void setActiveZoneMinutes(Integer activeZoneMinutes) {
        this.activeZoneMinutes = activeZoneMinutes;
    }

    public Integer getFatBurnActiveZoneMinutes() {
        return fatBurnActiveZoneMinutes;
    }

    public void setFatBurnActiveZoneMinutes(Integer fatBurnActiveZoneMinutes) {
        this.fatBurnActiveZoneMinutes = fatBurnActiveZoneMinutes;
    }

    public Integer getCardioActiveZoneMinutes() {
        return cardioActiveZoneMinutes;
    }

    public void setCardioActiveZoneMinutes(Integer cardioActiveZoneMinutes) {
        this.cardioActiveZoneMinutes = cardioActiveZoneMinutes;
    }

    public Integer getPeakActiveZoneMinutes() {
        return peakActiveZoneMinutes;
    }

    public void setPeakActiveZoneMinutes(Integer peakActiveZoneMinutes) {
        this.peakActiveZoneMinutes = peakActiveZoneMinutes;
    }

    public record AzmId(String userId, LocalDate date) implements java.io.Serializable { }
}
