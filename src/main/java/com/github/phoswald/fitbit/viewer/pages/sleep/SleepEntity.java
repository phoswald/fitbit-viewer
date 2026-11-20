package com.github.phoswald.fitbit.viewer.pages.sleep;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateOf;
import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.stageMinutesOf;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.add;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.google.api.health.v4.model.Sleep;
import com.google.api.health.v4.model.StageSummary;

@Entity
@Table(name = "fitbit_sleep_")
@NamedQuery(
        name = "SleepEntity.loadByUserIdAndDateRange",
        query = "SELECT s FROM SleepEntity s WHERE s.userId = :userId AND s.date >= :dateBeg AND s.date <= :dateEnd ORDER BY s.date"
)
@IdClass(SleepEntity.SleepId.class)
public class SleepEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "minutes_asleep_")
    private Integer minutesAsleep;

    @Column(name = "minutes_restless_")
    private Integer minutesRestless;

    @Column(name = "minutes_awake_")
    private Integer minutesAwake;

    @Column(name = "minutes_deep_")
    private Integer minutesDeep;

    @Column(name = "minutes_light_")
    private Integer minutesLight;

    @Column(name = "minutes_rem_")
    private Integer minutesRem;

    @Column(name = "minutes_wake_")
    private Integer minutesWake;

    public static SleepEntity create(String userId, Sleep sleep) {
        SleepEntity entity = new SleepEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(requireNonNull(dateOf(sleep), "date"));
        if(sleep.getType() == Sleep.TypeEnum.CLASSIC) {
            entity.setMinutesAsleep(stageMinutesOf(sleep, StageSummary.TypeEnum.ASLEEP));
            entity.setMinutesRestless(stageMinutesOf(sleep, StageSummary.TypeEnum.RESTLESS));
            entity.setMinutesAwake(stageMinutesOf(sleep, StageSummary.TypeEnum.AWAKE));
        }
        if(sleep.getType() == Sleep.TypeEnum.STAGES) {
            entity.setMinutesDeep(stageMinutesOf(sleep, StageSummary.TypeEnum.DEEP));
            entity.setMinutesLight(stageMinutesOf(sleep, StageSummary.TypeEnum.LIGHT));
            entity.setMinutesRem(stageMinutesOf(sleep, StageSummary.TypeEnum.REM));
            entity.setMinutesWake(stageMinutesOf(sleep, StageSummary.TypeEnum.AWAKE));
        }
        return entity;
    }

    public static SleepEntity merge(SleepEntity entity1, SleepEntity entity2) {
        SleepEntity entity = new SleepEntity();
        entity.setUserId(entity1.getUserId());
        entity.setDate(entity1.getDate());
        entity.setMinutesAsleep(add(entity1.getMinutesAsleep(), entity2.getMinutesAsleep()));
        entity.setMinutesRestless(add(entity1.getMinutesRestless(), entity2.getMinutesRestless()));
        entity.setMinutesAwake(add(entity1.getMinutesAwake(), entity2.getMinutesAwake()));
        entity.setMinutesDeep(add(entity1.getMinutesDeep(), entity2.getMinutesDeep()));
        entity.setMinutesLight(add(entity1.getMinutesLight(), entity2.getMinutesLight()));
        entity.setMinutesRem(add(entity1.getMinutesRem(),  entity2.getMinutesRem()));
        entity.setMinutesWake(add(entity1.getMinutesWake(), entity2.getMinutesWake()));
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

    public Integer getMinutesAsleep() {
        return minutesAsleep;
    }

    public void setMinutesAsleep(Integer minutesAsleep) {
        this.minutesAsleep = minutesAsleep;
    }

    public Integer getMinutesRestless() {
        return minutesRestless;
    }

    public void setMinutesRestless(Integer minutesRestless) {
        this.minutesRestless = minutesRestless;
    }

    public Integer getMinutesAwake() {
        return minutesAwake;
    }

    public void setMinutesAwake(Integer minutesAwake) {
        this.minutesAwake = minutesAwake;
    }

    public Integer getMinutesDeep() {
        return minutesDeep;
    }

    public void setMinutesDeep(Integer minutesDeep) {
        this.minutesDeep = minutesDeep;
    }

    public Integer getMinutesLight() {
        return minutesLight;
    }

    public void setMinutesLight(Integer minutesLight) {
        this.minutesLight = minutesLight;
    }

    public Integer getMinutesRem() {
        return minutesRem;
    }

    public void setMinutesRem(Integer minutesRem) {
        this.minutesRem = minutesRem;
    }

    public Integer getMinutesWake() {
        return minutesWake;
    }

    public void setMinutesWake(Integer minutesWake) {
        this.minutesWake = minutesWake;
    }

    public Integer getMinutesTotal() {
        return add(add(add(minutesDeep, minutesLight), minutesRem), minutesWake);
    }

    public Integer getMinutesSleep() {
        return add(add(minutesDeep, minutesLight), minutesRem);
    }

    public record SleepId(String userId, LocalDate date) implements Serializable { }
}
