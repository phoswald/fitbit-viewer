package com.github.phoswald.fitbit.viewer.healthapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.phoswald.fitbit.viewer.pages.activities.ActivityEntity;
import com.github.phoswald.fitbit.viewer.pages.activities.ActivityHeartRateZoneEntity;
import com.github.phoswald.fitbit.viewer.pages.azm.ActiveZoneMinutesEntity;
import com.github.phoswald.fitbit.viewer.pages.cardioscore.CardioScoreEntity;
import com.github.phoswald.fitbit.viewer.pages.heartrate.HeartRateEntity;
import com.github.phoswald.fitbit.viewer.pages.sleep.SleepEntity;
import com.google.api.health.v4.model.ActiveZoneMinutesRollupValue;
import com.google.api.health.v4.model.CivilDateTime;
import com.google.api.health.v4.model.DailyRestingHeartRate;
import com.google.api.health.v4.model.DailyRollupDataPoint;
import com.google.api.health.v4.model.DailyVO2Max;
import com.google.api.health.v4.model.DataPoint;
import com.google.api.health.v4.model.DataSource;
import com.google.api.health.v4.model.Date;
import com.google.api.health.v4.model.Device;
import com.google.api.health.v4.model.Exercise;
import com.google.api.health.v4.model.MetricsSummary;
import com.google.api.health.v4.model.SessionTimeInterval;
import com.google.api.health.v4.model.Sleep;
import com.google.api.health.v4.model.SleepSummary;
import com.google.api.health.v4.model.StageSummary;
import com.google.api.health.v4.model.TimeInHeartRateZones;

class HealthApiMappingTest {

    private static final String USER_ID = "user";
    private static final LocalDate DATE = LocalDate.of(2026, 3, 14);

    @Test
    void azm_allZones_totalIsSum() {
        var entity = ActiveZoneMinutesEntity.create(USER_ID, azmEntry("10", "20", "4"));

        assertEquals(DATE, entity.getDate());
        assertEquals(10, entity.getFatBurnAzm());
        assertEquals(20, entity.getCardioAzm());
        assertEquals(4, entity.getPeakAzm());
        assertEquals(34, entity.getTotalAzm());
    }

    @Test
    void azm_missingZones_totalIgnoresThem() {
        var entity = ActiveZoneMinutesEntity.create(USER_ID, azmEntry("10", null, null));

        assertEquals(DATE, entity.getDate());
        assertEquals(10, entity.getFatBurnAzm());
        assertNull(entity.getCardioAzm());
        assertNull(entity.getPeakAzm());
        assertEquals(10, entity.getTotalAzm());
    }

    @Test
    void azm_noValue_allNull() {
        var entity = ActiveZoneMinutesEntity.create(USER_ID, azmEntry(null, null, null).activeZoneMinutes(null));

        assertEquals(DATE, entity.getDate());
        assertNull(entity.getTotalAzm());
    }

    @Test
    void heartRate_mapsBeatsPerMinute() {
        var entity = HeartRateEntity.create(USER_ID, new DailyRestingHeartRate()
                .date(new Date().year(2026).month(3).day(14))
                .beatsPerMinute("58"));

        assertEquals(DATE, entity.getDate());
        assertEquals(58, entity.getRestingHeartRate());
    }

    @Test
    void cardioScore_alwaysStoredAsEstimated_regardlessOfFlag() {
        for (Boolean estimated : new Boolean[] { null, Boolean.FALSE, Boolean.TRUE }) {
            var entity = CardioScoreEntity.create(USER_ID, new DailyVO2Max()
                    .date(new Date().year(2026).month(3).day(14))
                    .vo2Max(new BigDecimal("46.5"))
                    .estimated(estimated));

            assertEquals(DATE, entity.getDate());
            assertEquals(46.5, entity.getScoreEstimated());
            assertNull(entity.getScoreMin());
            assertNull(entity.getScoreMax());
        }
    }

    @Test
    void sleep_stages_mapsAwakeToWake() {
        var entity = SleepEntity.create(USER_ID, sleep(Sleep.TypeEnum.STAGES,
                stage(StageSummary.TypeEnum.DEEP, "60"), stage(StageSummary.TypeEnum.LIGHT, "240"), stage(StageSummary.TypeEnum.REM, "90"), stage(StageSummary.TypeEnum.AWAKE, "30")));

        assertEquals(DATE, entity.getDate());
        assertEquals(60, entity.getMinutesDeep());
        assertEquals(240, entity.getMinutesLight());
        assertEquals(90, entity.getMinutesRem());
        assertEquals(30, entity.getMinutesWake());
        assertNull(entity.getMinutesAwake());
        assertNull(entity.getMinutesAsleep());
        assertEquals(420, entity.getMinutesTotal());
        assertEquals(390, entity.getMinutesSleep());
    }

    @Test
    void sleep_classic_mapsAwakeToAwake() {
        var entity = SleepEntity.create(USER_ID, sleep(Sleep.TypeEnum.CLASSIC,
                stage(StageSummary.TypeEnum.ASLEEP, "400"), stage(StageSummary.TypeEnum.RESTLESS, "20"), stage(StageSummary.TypeEnum.AWAKE, "5")));

        assertEquals(DATE, entity.getDate());
        assertEquals(400, entity.getMinutesAsleep());
        assertEquals(20, entity.getMinutesRestless());
        assertEquals(5, entity.getMinutesAwake());
        assertNull(entity.getMinutesWake());
        assertNull(entity.getMinutesDeep());
    }

    @Test
    void sleep_unspecifiedType_minutesNull() {
        var entity = SleepEntity.create(USER_ID, sleep(Sleep.TypeEnum.SLEEP_TYPE_UNSPECIFIED,
                stage(StageSummary.TypeEnum.AWAKE, "5")));

        assertEquals(DATE, entity.getDate());
        assertNull(entity.getMinutesAwake());
        assertNull(entity.getMinutesWake());
    }

    @Test
    void sleep_realResponseValues_usesLocalEndDate() {
        assertEquals(LocalDate.of(2026, 11, 5), sleepDate("2026-11-05T10:18:00Z", "3600s"));
    }

    @Test
    void sleep_positiveOffsetCrossingMidnight_usesLocalEndDate() {
        assertEquals(LocalDate.of(2026, 3, 15), sleepDate("2026-03-14T23:30:00Z", "3600s"));
    }

    @Test
    void sleep_negativeOffsetCrossingMidnight_usesLocalEndDate() {
        assertEquals(LocalDate.of(2026, 3, 13), sleepDate("2026-03-14T02:00:00Z", "-18000s"));
    }

    @Test
    void sleep_noEndTime_rejected() {
        var sleep = new Sleep()
                .interval(new SessionTimeInterval());

        assertThrows(NullPointerException.class, () -> SleepEntity.create(USER_ID, sleep));
    }

    @Test
    void activity_allFields_convertsUnits() {
        var entity = ActivityEntity.create(USER_ID, exercise()
                .dataSource(new DataSource()
                        .recordingMethod(DataSource.RecordingMethodEnum.ACTIVELY_MEASURED)
                        .device(new Device()
                                .displayName("Charge 6")
                                .formFactor(Device.FormFactorEnum.FITNESS_BAND))),
                List.of("label"));

        assertEquals(USER_ID, entity.getUserId());
        assertEquals(2026443605080188808L, entity.getLogId());
        assertEquals("Run", entity.getActivityName());
        assertNull(entity.getActivityTypeId());
        assertEquals(DATE, entity.getDate());
        assertEquals(OffsetDateTime.parse("2026-03-14T09:00:00+01:00"), entity.getBegDateTime());
        assertEquals(OffsetDateTime.parse("2026-03-14T09:30:00+01:00"), entity.getEndDateTime());
        assertEquals(30.0, entity.getDurationMinutes());
        assertEquals(27.5, entity.getDurationActiveMinutes());
        assertEquals(5.5, entity.getDistance());
        assertEquals(6000, entity.getSteps());
        assertEquals(351, entity.getCalories());
        assertEquals(10.8, entity.getSpeed(), 0.000001);
        assertEquals(333.0, entity.getPace(), 0.000001);
        assertEquals(150, entity.getAverageHeartRate());
        assertEquals(40, entity.getActiveZoneMinutes());
        assertEquals("ACTIVELY_MEASURED", entity.getLogType());
        assertEquals("Charge 6", entity.getSourceName());
        assertEquals("FITNESS_BAND", entity.getSourceType());
        assertNull(entity.getSourceUrl());
        assertNull(entity.getSourceFeatures());
        assertEquals(List.of("label"), entity.getLabels());
        assertTrue(entity.getActivityLevels().isEmpty());
    }

    @Test
    void activity_zones_minutesOnlyAndMissingZonesSkipped() {
        var entity = ActivityEntity.create(USER_ID, exercise(), List.of());

        var zones = entity.getHeartRateZones();
        assertEquals(List.of("Out of Range", "Fat Burn", "Peak"), zones.stream().map(ActivityHeartRateZoneEntity::getZoneName).toList());
        assertEquals(List.of(5, 20, 1), zones.stream().map(ActivityHeartRateZoneEntity::getZoneMinutes).toList());
        assertEquals(2026443605080188808L, zones.get(0).getLogId());
        assertEquals(USER_ID, zones.get(0).getUserId());
        assertEquals(30, zones.get(0).getHeartRateMin());
        assertEquals(98, zones.get(0).getHeartRateMax());
        assertNull(zones.get(0).getCalories());
    }

    @Test
    void activity_offsetCrossingMidnight_usesLocalStartDate() {
        var dataPoint = exercise();
        dataPoint.getExercise().getInterval().startTime("2026-03-14T02:00:00Z").startUtcOffset("-18000s");

        assertEquals(LocalDate.of(2026, 3, 13), ActivityEntity.create(USER_ID, dataPoint, List.of()).getDate());
    }

    @Test
    void activity_noSummaryAndNoDataSource_fieldsNull() {
        var dataPoint = exercise();
        dataPoint.getExercise().metricsSummary(null);

        var entity = ActivityEntity.create(USER_ID, dataPoint, List.of());

        assertNull(entity.getCalories());
        assertNull(entity.getDistance());
        assertNull(entity.getLogType());
        assertNull(entity.getSourceName());
        assertTrue(entity.getHeartRateZones().isEmpty());
    }

    @Test
    void activity_nonNumericId_rejected() {
        var dataPoint = exercise().name("users/me/dataTypes/exercise/dataPoints/morning-trail-run-123456");

        assertThrows(NumberFormatException.class, () -> ActivityEntity.create(USER_ID, dataPoint, List.of()));
    }

    @Test
    void activity_isAutoDetected_oldAndNewLogTypes() {
        var entity = new ActivityEntity();
        for (String logType : List.of("auto_detected", "PASSIVELY_MEASURED")) {
            entity.setLogType(logType);
            assertTrue(entity.isLogTypeAuto(), logType);
        }
        for (String logType : List.of("tracker", "MANUAL", "ACTIVELY_MEASURED")) {
            entity.setLogType(logType);
            assertFalse(entity.isLogTypeAuto(), logType);
        }
        entity.setLogType(null);
        assertFalse(entity.isLogTypeAuto());
    }

    private static DataPoint exercise() {
        return new DataPoint()
                .name("users/me/dataTypes/exercise/dataPoints/2026443605080188808")
                .exercise(new Exercise()
                        .displayName("Run")
                        .activeDuration("1650s")
                        .interval(new SessionTimeInterval()
                                .startTime("2026-03-14T08:00:00Z")
                                .startUtcOffset("3600s")
                                .endTime("2026-03-14T08:30:00Z")
                                .endUtcOffset("3600s"))
                        .metricsSummary(new MetricsSummary()
                                .distanceMillimeters(new BigDecimal("5500000"))
                                .steps("6000")
                                .caloriesKcal(new BigDecimal("350.6"))
                                .averageSpeedMillimetersPerSecond(new BigDecimal("3000"))
                                .averagePaceSecondsPerMeter(new BigDecimal("0.333"))
                                .averageHeartRateBeatsPerMinute("150")
                                .activeZoneMinutes("40")
                                .heartRateZoneDurations(new TimeInHeartRateZones()
                                        .lightTime("300s")
                                        .moderateTime("1200s")
                                        .peakTime("60s"))));
    }

    private static DailyRollupDataPoint azmEntry(String fatBurn, String cardio, String peak) {
        return new DailyRollupDataPoint()
                .civilStartTime(new CivilDateTime()
                        .date(new Date().year(2026).month(3).day(14)))
                .activeZoneMinutes(new ActiveZoneMinutesRollupValue()
                        .sumInFatBurnHeartZone(fatBurn)
                        .sumInCardioHeartZone(cardio)
                        .sumInPeakHeartZone(peak));
    }

    private static LocalDate sleepDate(String endTime, String endUtcOffset) {
        var sleep = new Sleep()
                .interval(new SessionTimeInterval()
                        .endTime(endTime)
                        .endUtcOffset(endUtcOffset));
        return SleepEntity.create(USER_ID, sleep).getDate();
    }

    private static Sleep sleep(
            Sleep.TypeEnum type,
            StageSummary... stages) {
        return new Sleep()
                .type(type)
                .interval(new SessionTimeInterval()
                        .endTime("2026-03-14T07:00:00Z")
                        .endUtcOffset("3600s"))
                .summary(new SleepSummary()
                        .stagesSummary(List.of(stages)));
    }

    private static StageSummary stage(StageSummary.TypeEnum type, String minutes) {
        return new StageSummary().type(type).minutes(minutes);
    }
}
