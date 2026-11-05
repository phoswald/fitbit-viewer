package com.github.phoswald.fitbit.viewer.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInner;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInnerActiveZoneMinutes;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInnerCivilStartTime;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInnerCivilStartTimeDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRateDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyVo2Max;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyVo2MaxDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleep;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepInterval;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepSummary;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner.TypeEnum;

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
        var entity = HeartRateEntity.create(USER_ID, new ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRate()
                .date(new ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRateDate().year(2026).month(3).day(14))
                .beatsPerMinute("58"));

        assertEquals(DATE, entity.getDate());
        assertEquals(58, entity.getRestingHeartRate());
    }

    @Test
    void cardioScore_alwaysStoredAsEstimated_regardlessOfFlag() {
        for (Boolean estimated : new Boolean[] { null, Boolean.FALSE, Boolean.TRUE }) {
            var entity = CardioScoreEntity.create(USER_ID, new ReconcileDataPointsResponseDataPointsInnerDailyVo2Max()
                    .date(new ReconcileDataPointsResponseDataPointsInnerDailyVo2MaxDate().year(2026).month(3).day(14))
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
        var entity = SleepEntity.create(USER_ID, sleep(ReconcileDataPointsResponseDataPointsInnerSleep.TypeEnum.STAGES,
                stage(TypeEnum.DEEP, "60"), stage(TypeEnum.LIGHT, "240"), stage(TypeEnum.REM, "90"), stage(TypeEnum.AWAKE, "30")));

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
        var entity = SleepEntity.create(USER_ID, sleep(ReconcileDataPointsResponseDataPointsInnerSleep.TypeEnum.CLASSIC,
                stage(TypeEnum.ASLEEP, "400"), stage(TypeEnum.RESTLESS, "20"), stage(TypeEnum.AWAKE, "5")));

        assertEquals(DATE, entity.getDate());
        assertEquals(400, entity.getMinutesAsleep());
        assertEquals(20, entity.getMinutesRestless());
        assertEquals(5, entity.getMinutesAwake());
        assertNull(entity.getMinutesWake());
        assertNull(entity.getMinutesDeep());
    }

    @Test
    void sleep_unspecifiedType_minutesNull() {
        var entity = SleepEntity.create(USER_ID, sleep(ReconcileDataPointsResponseDataPointsInnerSleep.TypeEnum.SLEEP_TYPE_UNSPECIFIED,
                stage(TypeEnum.AWAKE, "5")));

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
        var sleep = new ReconcileDataPointsResponseDataPointsInnerSleep()
                .interval(new ReconcileDataPointsResponseDataPointsInnerSleepInterval());

        assertThrows(NullPointerException.class, () -> SleepEntity.create(USER_ID, sleep));
    }

    private static DailyRollUpDataPointsResponseRollupDataPointsInner azmEntry(String fatBurn, String cardio, String peak) {
        return new DailyRollUpDataPointsResponseRollupDataPointsInner()
                .civilStartTime(new DailyRollUpDataPointsResponseRollupDataPointsInnerCivilStartTime()
                        .date(new DailyRollUpDataPointsResponseRollupDataPointsInnerCivilStartTimeDate().year(2026).month(3).day(14)))
                .activeZoneMinutes(new DailyRollUpDataPointsResponseRollupDataPointsInnerActiveZoneMinutes()
                        .sumInFatBurnHeartZone(fatBurn)
                        .sumInCardioHeartZone(cardio)
                        .sumInPeakHeartZone(peak));
    }

    private static LocalDate sleepDate(String endTime, String endUtcOffset) {
        var sleep = new ReconcileDataPointsResponseDataPointsInnerSleep()
                .interval(new ReconcileDataPointsResponseDataPointsInnerSleepInterval()
                        .endTime(endTime)
                        .endUtcOffset(endUtcOffset));
        return SleepEntity.create(USER_ID, sleep).getDate();
    }

    private static ReconcileDataPointsResponseDataPointsInnerSleep sleep(
            ReconcileDataPointsResponseDataPointsInnerSleep.TypeEnum type,
            ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner... stages) {
        return new ReconcileDataPointsResponseDataPointsInnerSleep()
                .type(type)
                .interval(new ReconcileDataPointsResponseDataPointsInnerSleepInterval()
                        .endTime("2026-03-14T07:00:00Z")
                        .endUtcOffset("3600s"))
                .summary(new ReconcileDataPointsResponseDataPointsInnerSleepSummary()
                        .stagesSummary(List.of(stages)));
    }

    private static ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner stage(TypeEnum type, String minutes) {
        return new ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner().type(type).minutes(minutes);
    }
}
