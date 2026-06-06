package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.add;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseDate;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.SleepApiClient;

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

    public static SleepEntity create(String userId, SleepApiClient.SleepEntry entry) {
        SleepEntity entity = new SleepEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(parseDate(requireNonNull(entry.dateOfSleep(), "dateOfSleep")));
        if(entry.levels() != null && entry.levels().summary() != null) {
            if(Objects.equals(entry.type(), "classic")) {
                entity.setMinutesAsleep(getMinutes(entry.levels().summary().asleep()));
                entity.setMinutesRestless(getMinutes(entry.levels().summary().restless()));
                entity.setMinutesAwake(getMinutes(entry.levels().summary().awake()));
            }
            if(Objects.equals(entry.type(), "stages")) {
                entity.setMinutesDeep(getMinutes(entry.levels().summary().deep()));
                entity.setMinutesLight(getMinutes(entry.levels().summary().light()));
                entity.setMinutesRem(getMinutes(entry.levels().summary().rem()));
                entity.setMinutesWake(getMinutes(entry.levels().summary().wake()));
            }
        }
        return entity;
    }

    private static Integer getMinutes(SleepApiClient.SleepLevelDetail detail) {
        return detail == null ? null : detail.minutes();
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

    public Integer getMinutesTotal() {
        return add(add(add(minutesDeep, minutesLight), minutesRem), minutesWake);
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

    public record SleepId(String userId, LocalDate date) implements Serializable { }
}
