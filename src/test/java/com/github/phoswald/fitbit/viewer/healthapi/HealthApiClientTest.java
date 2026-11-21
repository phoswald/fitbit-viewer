package com.github.phoswald.fitbit.viewer.healthapi;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.api.health.v4.model.DailyVO2Max.CardioFitnessLevelEnum;
import com.google.api.health.v4.model.Exercise.ExerciseTypeEnum;

import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Tests HealthApiClient with WireMock and sample data recorded by {@code query-api-samples.sh}.
 */
@QuarkusTest
@ConnectWireMock
class HealthApiClientTest {

    private static final SessionData SESSION = SessionData.builder().accessToken("sample-auth-token").build();
    private static final LocalDate DATE_BEG = LocalDate.of(2026, 9, 1);
    private static final LocalDate DATE_END = LocalDate.of(2026, 9, 30);

    @Inject
    HealthApiClient testee;

    WireMock wiremock;

    @BeforeEach
    void resetWireMock() {
        wiremock.resetMappings();
        wiremock.resetRequests();
    }

    @Test
    void getProfile_valid_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/profile")))
                .willReturn(okJson(resource("profile.response.json"))));

        var profile = testee.getProfile(SESSION);

        assertEquals("users/4981657191938844637/profile", profile.getName());
        assertEquals(49, profile.getAge());
        assertEquals(2019, profile.getMembershipStartDate().getYear());
        assertEquals(12, profile.getMembershipStartDate().getMonth());
        assertEquals(4, profile.getMembershipStartDate().getDay());
        assertEquals(738, profile.getUserConfiguredWalkingStrideLengthMm());
        assertEquals(1021, profile.getUserConfiguredRunningStrideLengthMm());
    }

    @Test
    void getDailyRollupDataPoints_steps_success() {
        wiremock.register(authorized(post(urlPathEqualTo("/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp")))
                .withRequestBody(equalToJson(resource("steps.request.json")))
                .willReturn(okJson(resource("steps.response.json"))));

        var dataPoints = testee.getDailyRollupDataPoints(SESSION, "steps", DATE_BEG, DATE_END);

        assertEquals(30, dataPoints.size());
        assertEquals(30, dataPoints.getFirst().getCivilStartTime().getDate().getDay());
        assertEquals("29578", dataPoints.getFirst().getSteps().getCountSum());
        assertEquals("9999", dataPoints.getLast().getSteps().getCountSum());
    }

    @Test
    void getDailyRollupDataPoints_stepsPaged_allCollected() {
        wiremock.register(authorized(post(urlPathEqualTo("/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp")))
                .withRequestBody(matchingJsonPath("$.pageToken", absent()))
                .willReturn(okJson(resource("steps-paged.response.json"))));
        wiremock.register(authorized(post(urlPathEqualTo("/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp")))
                .withRequestBody(matchingJsonPath("$.pageToken", equalTo("sample-page-token-1")))
                .willReturn(okJson(resource("steps.response.json"))));

        var dataPoints = testee.getDailyRollupDataPoints(SESSION, "steps", DATE_BEG, DATE_END);

        assertEquals(1 + 30, dataPoints.size());
        assertEquals("1234", dataPoints.getFirst().getSteps().getCountSum());
        assertEquals("29578", dataPoints.get(1).getSteps().getCountSum());
        assertEquals("9999", dataPoints.getLast().getSteps().getCountSum());

        wiremock.verifyThat(2, postRequestedFor(urlPathEqualTo("/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp")));
    }

    @Test
    void getDailyRollupDataPoints_azm_success() {
        wiremock.register(authorized(post(urlPathEqualTo("/v4/users/me/dataTypes/active-zone-minutes/dataPoints:dailyRollUp")))
                .withRequestBody(equalToJson(resource("azm.request.json")))
                .willReturn(okJson(resource("azm.response.json"))));

        var dataPoints = testee.getDailyRollupDataPoints(SESSION, "active-zone-minutes", DATE_BEG, DATE_END);

        assertEquals(25, dataPoints.size());
        assertEquals("26", dataPoints.getFirst().getActiveZoneMinutes().getSumInFatBurnHeartZone());
        assertEquals("130", dataPoints.getFirst().getActiveZoneMinutes().getSumInCardioHeartZone());
        assertEquals("64", dataPoints.getFirst().getActiveZoneMinutes().getSumInPeakHeartZone());
        assertEquals("10", dataPoints.getLast().getActiveZoneMinutes().getSumInFatBurnHeartZone());
        assertEquals("92", dataPoints.getLast().getActiveZoneMinutes().getSumInCardioHeartZone());
        assertEquals("2", dataPoints.getLast().getActiveZoneMinutes().getSumInPeakHeartZone());
    }

    @Test
    void getReconciledDataPoints_heartrate_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/daily-resting-heart-rate/dataPoints:reconcile")))
                .withQueryParam("filter", equalTo("daily_resting_heart_rate.date >= \"2026-09-01\" AND daily_resting_heart_rate.date < \"2026-10-01\""))
                .willReturn(okJson(resource("heartrate.response.json"))));

        var dataPoints = testee.getReconciledDataPoints(SESSION, "daily-resting-heart-rate", "daily_resting_heart_rate.date", DATE_BEG, DATE_END);

        assertEquals(30, dataPoints.size());
        assertEquals(30, dataPoints.getFirst().getDailyRestingHeartRate().getDate().getDay());
        assertEquals("52", dataPoints.getFirst().getDailyRestingHeartRate().getBeatsPerMinute());
        assertEquals(1, dataPoints.getLast().getDailyRestingHeartRate().getDate().getDay());
        assertEquals("53", dataPoints.getLast().getDailyRestingHeartRate().getBeatsPerMinute());
    }

    @Test
    void getReconciledDataPoints_heartratePaged_allCollected() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/daily-resting-heart-rate/dataPoints:reconcile")))
                .withQueryParam("pageToken", absent())
                .willReturn(okJson(resource("heartrate-paged.response.json"))));
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/daily-resting-heart-rate/dataPoints:reconcile")))
                .withQueryParam("pageToken", equalTo("sample-page-token-2"))
                .willReturn(okJson(resource("heartrate.response.json"))));

        var dataPoints = testee.getReconciledDataPoints(SESSION, "daily-resting-heart-rate", "daily_resting_heart_rate.date", DATE_BEG, DATE_END);

        assertEquals(1 + 30, dataPoints.size());
        assertEquals("60", dataPoints.getFirst().getDailyRestingHeartRate().getBeatsPerMinute());
        assertEquals(30, dataPoints.get(1).getDailyRestingHeartRate().getDate().getDay());
        assertEquals("52", dataPoints.get(1).getDailyRestingHeartRate().getBeatsPerMinute());
        assertEquals(1, dataPoints.getLast().getDailyRestingHeartRate().getDate().getDay());
        assertEquals("53", dataPoints.getLast().getDailyRestingHeartRate().getBeatsPerMinute());

        wiremock.verifyThat(2, getRequestedFor(urlPathEqualTo("/v4/users/me/dataTypes/daily-resting-heart-rate/dataPoints:reconcile")));
    }

    @Test
    void getReconciledDataPoints_cardioscore_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/daily-vo2-max/dataPoints:reconcile")))
                .withQueryParam("filter", equalTo("daily_vo2_max.date >= \"2026-09-01\" AND daily_vo2_max.date < \"2026-10-01\""))
                .willReturn(okJson(resource("cardioscore.response.json"))));

        var dataPoints = testee.getReconciledDataPoints(SESSION, "daily-vo2-max", "daily_vo2_max.date", DATE_BEG, DATE_END);

        assertEquals(30, dataPoints.size());
        assertEquals(30, dataPoints.getFirst().getDailyVo2Max().getDate().getDay());
        assertEquals("49.144159904536963", dataPoints.getFirst().getDailyVo2Max().getVo2Max().toPlainString());
        assertEquals(CardioFitnessLevelEnum.VERY_GOOD, dataPoints.getFirst().getDailyVo2Max().getCardioFitnessLevel());
        assertEquals(1, dataPoints.getLast().getDailyVo2Max().getDate().getDay());
        assertEquals("50.501831757983894", dataPoints.getLast().getDailyVo2Max().getVo2Max().toPlainString());
        assertEquals(CardioFitnessLevelEnum.EXCELLENT, dataPoints.getLast().getDailyVo2Max().getCardioFitnessLevel());
    }

    @Test
    void getReconciledDataPoints_sleep_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/sleep/dataPoints:reconcile")))
                .withQueryParam("filter", equalTo("sleep.interval.civil_end_time >= \"2026-09-01\" AND sleep.interval.civil_end_time < \"2026-10-01\""))
                .withQueryParam("pageToken", absent())
                .willReturn(okJson(resource("sleep.response.json"))));
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/sleep/dataPoints:reconcile")))
                .withQueryParam("filter", equalTo("sleep.interval.civil_end_time >= \"2026-09-01\" AND sleep.interval.civil_end_time < \"2026-10-01\""))
                .withQueryParam("pageToken", equalTo("chcKCYIBBgjU2bnVBgoKEMjQseKj75-RMQ"))
                .willReturn(okJson("{}")));

        var dataPoints = testee.getReconciledDataPoints(SESSION, "sleep", "sleep.interval.civil_end_time", DATE_BEG, DATE_END);

        assertEquals(13, dataPoints.size());
        assertEquals("2026-09-29T23:23:00Z", dataPoints.getFirst().getSleep().getInterval().getStartTime());
        assertEquals("2026-09-30T09:40:00Z", dataPoints.getFirst().getSleep().getInterval().getEndTime());
        assertEquals("7200s", dataPoints.getFirst().getSleep().getInterval().getEndUtcOffset());
        assertEquals("2026-09-18T23:11:00Z", dataPoints.getLast().getSleep().getInterval().getStartTime());
        assertEquals("2026-09-19T09:07:00Z", dataPoints.getLast().getSleep().getInterval().getEndTime());
        assertEquals("7200s", dataPoints.getLast().getSleep().getInterval().getEndUtcOffset());

        wiremock.verifyThat(2, getRequestedFor(urlPathEqualTo("/v4/users/me/dataTypes/sleep/dataPoints:reconcile")));
    }

    @Test
    void getListedDataPoints_activity_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/exercise/dataPoints")))
                .withQueryParam("filter", equalTo("exercise.interval.civil_start_time >= \"2026-09-01\" AND exercise.interval.civil_start_time < \"2026-10-01\""))
                .withQueryParam("pageToken", absent())
                .willReturn(okJson(resource("activity.response.json"))));
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/exercise/dataPoints")))
                .withQueryParam("filter", equalTo("exercise.interval.civil_start_time >= \"2026-09-01\" AND exercise.interval.civil_start_time < \"2026-10-01\""))
                .withQueryParam("pageToken", equalTo("25"))
                .willReturn(okJson("{}")));

        var dataPoints = testee.getListedDataPoints(SESSION, "exercise", "exercise.interval.civil_start_time", DATE_BEG, DATE_END);

        assertEquals(25, dataPoints.size());
        assertEquals("users/4981657191938844637/dataTypes/exercise/dataPoints/3163336676098067440", dataPoints.getFirst().getName());
        assertEquals("Charge 6", dataPoints.getFirst().getDataSource().getDevice().getDisplayName());
        assertEquals("Gehen", dataPoints.getFirst().getExercise().getDisplayName());
        assertEquals(ExerciseTypeEnum.WALKING, dataPoints.getFirst().getExercise().getExerciseType());
        assertEquals("2026-09-30T14:16:52Z", dataPoints.getFirst().getExercise().getInterval().getStartTime());
        assertEquals("users/4981657191938844637/dataTypes/exercise/dataPoints/3674140269671331864", dataPoints.getLast().getName());
        assertEquals("Charge 6", dataPoints.getLast().getDataSource().getDevice().getDisplayName());
        assertEquals("Spinning", dataPoints.getLast().getExercise().getDisplayName());
        assertEquals(ExerciseTypeEnum.SPINNING, dataPoints.getLast().getExercise().getExerciseType());
        assertEquals("2026-09-07T15:44:57Z", dataPoints.getLast().getExercise().getInterval().getStartTime());

        wiremock.verifyThat(2, getRequestedFor(urlPathEqualTo("/v4/users/me/dataTypes/exercise/dataPoints")));
    }

    @Test
    void getFirstDataPoint_height_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/height/dataPoints")))
                .withQueryParam("pageSize", equalTo("1"))
                .willReturn(okJson(resource("profile-height.response.json"))));

        var dataPoint = testee.getFirstDataPoint(SESSION, "height");

        assertNotNull(dataPoint);
        assertEquals("1780", dataPoint.getHeight().getHeightMillimeters());
    }

    @Test
    void getFirstDataPoint_weight_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/weight/dataPoints")))
                .withQueryParam("pageSize", equalTo("1"))
                .willReturn(okJson(resource("profile-weight.response.json"))));

        var dataPoint = testee.getFirstDataPoint(SESSION, "weight");

        assertNotNull(dataPoint);
        assertEquals("81000", dataPoint.getWeight().getWeightGrams().toPlainString());
    }

    @Test
    void getFirstDataPoint_empty_null() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/weight/dataPoints")))
                .willReturn(okJson("{}")));

        assertNull(testee.getFirstDataPoint(SESSION, "weight"));
    }

    @Test
    void getExportExerciseTcx_valid_success() {
        wiremock.register(authorized(get(urlPathEqualTo("/v4/users/me/dataTypes/exercise/dataPoints/3163336676098067440:exportExerciseTcx")))
                .willReturn(okJson(resource("tcx.response.json"))));

        String tcx = testee.getExportExerciseTcx(SESSION, 3163336676098067440L);

        assertTrue(tcx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
        assertTrue(tcx.contains("<TrainingCenterDatabase"));
    }
    
    private static MappingBuilder authorized(MappingBuilder mapping) {
        return mapping.withHeader("Authorization", equalTo("Bearer sample-auth-token"));
    }

    private static String resource(String name) {
        try (InputStream stream = HealthApiClientTest.class.getResourceAsStream("/api-samples/" + name)) {
            if (stream == null) {
                throw new IllegalArgumentException("Sample not found: " + name);
            }
            return new String(stream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
