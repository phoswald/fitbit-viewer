package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseDate;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.LocalDate;

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
        entity.addEntry(entry);
        return entity;
    }

    public void addEntry(SleepApiClient.SleepEntry entry) {
        if (entry.levels() == null || entry.levels().summary() == null) {
            return;
        }
        SleepApiClient.SleepLevelsSummary s = entry.levels().summary();
        if ("classic".equals(entry.type())) {
            minutesAsleep   = add(minutesAsleep,   s.asleep()   != null ? s.asleep().minutes()   : null);
            minutesRestless = add(minutesRestless,  s.restless() != null ? s.restless().minutes() : null);
            minutesAwake    = add(minutesAwake,     s.awake()    != null ? s.awake().minutes()    : null);
        } else if ("stages".equals(entry.type())) {
            minutesDeep  = add(minutesDeep,  s.deep()  != null ? s.deep().minutes()  : null);
            minutesLight = add(minutesLight, s.light() != null ? s.light().minutes() : null);
            minutesRem   = add(minutesRem,   s.rem()   != null ? s.rem().minutes()   : null);
            minutesWake  = add(minutesWake,  s.wake()  != null ? s.wake().minutes()  : null);
        }
    }

    private static Integer add(Integer existing, Integer delta) {
        if (delta == null) return existing;
        return existing == null ? delta : existing + delta;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getMinutesAsleep() { return minutesAsleep; }
    public void setMinutesAsleep(Integer minutesAsleep) { this.minutesAsleep = minutesAsleep; }

    public Integer getMinutesRestless() { return minutesRestless; }
    public void setMinutesRestless(Integer minutesRestless) { this.minutesRestless = minutesRestless; }

    public Integer getMinutesAwake() { return minutesAwake; }
    public void setMinutesAwake(Integer minutesAwake) { this.minutesAwake = minutesAwake; }

    public Integer getMinutesDeep() { return minutesDeep; }
    public void setMinutesDeep(Integer minutesDeep) { this.minutesDeep = minutesDeep; }

    public Integer getMinutesLight() { return minutesLight; }
    public void setMinutesLight(Integer minutesLight) { this.minutesLight = minutesLight; }

    public Integer getMinutesRem() { return minutesRem; }
    public void setMinutesRem(Integer minutesRem) { this.minutesRem = minutesRem; }

    public Integer getMinutesWake() { return minutesWake; }
    public void setMinutesWake(Integer minutesWake) { this.minutesWake = minutesWake; }

    public record SleepId(String userId, LocalDate date) implements Serializable { }
}
