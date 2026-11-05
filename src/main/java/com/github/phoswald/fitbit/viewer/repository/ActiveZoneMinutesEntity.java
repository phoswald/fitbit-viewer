package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateOf;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.add;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseInteger;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInner;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInnerActiveZoneMinutes;

@Entity
@Table(name = "fitbit_active_zone_minutes_")
@NamedQuery(
        name = "ActiveZoneMinutesEntity.loadByUserIdAndDateRange",
        query = "SELECT a FROM ActiveZoneMinutesEntity a WHERE a.userId = :userId AND a.date >= :dateBeg AND a.date <= :dateEnd ORDER BY a.date"
)
@IdClass(ActiveZoneMinutesEntity.AzmId.class)
public class ActiveZoneMinutesEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "total_azm_")
    private Integer totalAzm;

    @Column(name = "fat_burn_azm_")
    private Integer fatBurnAzm;

    @Column(name = "cardio_azm_")
    private Integer cardioAzm;

    @Column(name = "peak_azm_")
    private Integer peakAzm;

    public static ActiveZoneMinutesEntity create(String userId, DailyRollUpDataPointsResponseRollupDataPointsInner dataPoint) {
        ActiveZoneMinutesEntity entity = new ActiveZoneMinutesEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(requireNonNull(dateOf(dataPoint), "date"));
        DailyRollUpDataPointsResponseRollupDataPointsInnerActiveZoneMinutes activeZoneMinutes = dataPoint.getActiveZoneMinutes();
        if(activeZoneMinutes != null) {
            entity.setFatBurnAzm(parseInteger(activeZoneMinutes.getSumInFatBurnHeartZone()));
            entity.setCardioAzm(parseInteger(activeZoneMinutes.getSumInCardioHeartZone()));
            entity.setPeakAzm(parseInteger(activeZoneMinutes.getSumInPeakHeartZone()));
            // The Google Health API has no total
            entity.setTotalAzm(add(add(entity.getFatBurnAzm(), entity.getCardioAzm()), entity.getPeakAzm()));
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

    public Integer getTotalAzm() {
        return totalAzm;
    }

    public void setTotalAzm(Integer totalAzm) {
        this.totalAzm = totalAzm;
    }

    public Integer getFatBurnAzm() {
        return fatBurnAzm;
    }

    public void setFatBurnAzm(Integer fatBurnAzm) {
        this.fatBurnAzm = fatBurnAzm;
    }

    public Integer getCardioAzm() {
        return cardioAzm;
    }

    public void setCardioAzm(Integer cardioAzm) {
        this.cardioAzm = cardioAzm;
    }

    public Integer getPeakAzm() {
        return peakAzm;
    }

    public void setPeakAzm(Integer peakAzm) {
        this.peakAzm = peakAzm;
    }

    public record AzmId(String userId, LocalDate date) implements java.io.Serializable { }
}
