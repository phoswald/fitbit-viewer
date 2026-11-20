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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.api.health.v4.model.ListDataPointsResponseDataPointsInnerExercise.ExerciseTypeEnum;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyVo2Max.CardioFitnessLevelEnum;

import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Tests {@link HealthApiClient} against WireMock, using the samples recorded by {@code query-api-samples.sh}.
 * The stubbed requests mirror the requests in that script.
 */
@QuarkusTest
@ConnectWireMock
class HealthApiClientTest {

    private static final String BASE_PATH = "/v4/users/me";
    private static final SessionData SESSION = SessionData.builder().accessToken("test-token").build();
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
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/profile")))
                .willReturn(okJson(sample("profile.response.json"))));

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
    void getListFirst_height_success() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/height/dataPoints")))
                .withQueryParam("pageSize", equalTo("1"))
                .willReturn(okJson(sample("profile-height.response.json"))));

        var dataPoint = testee.getListFirst(SESSION, "height");

        assertNotNull(dataPoint);
        assertEquals("1780", dataPoint.getHeight().getHeightMillimeters());
    }

    @Test
    void getListFirst_weight_success() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/weight/dataPoints")))
                .withQueryParam("pageSize", equalTo("1"))
                .willReturn(okJson(sample("profile-weight.response.json"))));

        var dataPoint = testee.getListFirst(SESSION, "weight");

        assertNotNull(dataPoint);
        assertEquals(0, new BigDecimal("81000").compareTo(dataPoint.getWeight().getWeightGrams()));
    }

    @Test
    void getListFirst_empty_null() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/weight/dataPoints")))
                .willReturn(okJson("{}")));

        assertNull(testee.getListFirst(SESSION, "weight"));
    }

    @Test
    void postDailyRollup_steps_success() {
        wiremock.register(authorized(post(urlPathEqualTo(BASE_PATH + "/dataTypes/steps/dataPoints:dailyRollUp")))
                .withRequestBody(equalToJson(sample("steps.request.json")))
                .willReturn(okJson(sample("steps.response.json"))));

        var dataPoints = testee.postDailyRollup(SESSION, "steps", DATE_BEG, DATE_END);

        assertEquals(30, dataPoints.size());
        var first = dataPoints.getFirst();
        assertEquals(30, first.getCivilStartTime().getDate().getDay());
        assertEquals("29578", first.getSteps().getCountSum());
    }

    @Test
    void postDailyRollup_azm_success() {
        wiremock.register(authorized(post(urlPathEqualTo(BASE_PATH + "/dataTypes/active-zone-minutes/dataPoints:dailyRollUp")))
                .withRequestBody(equalToJson(sample("azm.request.json")))
                .willReturn(okJson(sample("azm.response.json"))));

        var dataPoints = testee.postDailyRollup(SESSION, "active-zone-minutes", DATE_BEG, DATE_END);

        assertEquals(25, dataPoints.size());
        var azm = dataPoints.getFirst().getActiveZoneMinutes();
        assertEquals("26", azm.getSumInFatBurnHeartZone());
        assertEquals("130", azm.getSumInCardioHeartZone());
        assertEquals("64", azm.getSumInPeakHeartZone());
    }

    @Test
    void postDailyRollup_multiplePages_allCollected() {
        String path = BASE_PATH + "/dataTypes/steps/dataPoints:dailyRollUp";
        wiremock.register(authorized(post(urlPathEqualTo(path)))
                .withRequestBody(matchingJsonPath("$.pageToken", absent()))
                .willReturn(okJson("""
                        { "rollupDataPoints": [ { "steps": { "countSum": "1234" } } ], "nextPageToken": "p2" }
                        """)));
        wiremock.register(authorized(post(urlPathEqualTo(path)))
                .withRequestBody(matchingJsonPath("$.pageToken", equalTo("p2")))
                .willReturn(okJson(sample("steps.response.json"))));

        var dataPoints = testee.postDailyRollup(SESSION, "steps", DATE_BEG, DATE_END);

        assertEquals(1 + 30, dataPoints.size());
        assertEquals("1234", dataPoints.getFirst().getSteps().getCountSum());
        wiremock.verifyThat(2, postRequestedFor(urlPathEqualTo(path)));
    }

    @Test
    void getReconcile_heartrate_success() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/daily-resting-heart-rate/dataPoints:reconcile")))
                .withQueryParam("filter", equalTo("daily_resting_heart_rate.date >= \"2026-09-01\" AND daily_resting_heart_rate.date < \"2026-10-01\""))
                .willReturn(okJson(sample("heartrate.response.json"))));

        var dataPoints = testee.getReconcile(SESSION, "daily-resting-heart-rate", "daily_resting_heart_rate.date", DATE_BEG, DATE_END);

        assertEquals(30, dataPoints.size());
        var heartRate = dataPoints.getFirst().getDailyRestingHeartRate();
        assertEquals(30, heartRate.getDate().getDay());
        assertEquals("52", heartRate.getBeatsPerMinute());
    }

    @Test
    void getReconcile_cardioscore_success() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/daily-vo2-max/dataPoints:reconcile")))
                .withQueryParam("filter", equalTo("daily_vo2_max.date >= \"2026-09-01\" AND daily_vo2_max.date < \"2026-10-01\""))
                .willReturn(okJson(sample("cardioscore.response.json"))));

        var dataPoints = testee.getReconcile(SESSION, "daily-vo2-max", "daily_vo2_max.date", DATE_BEG, DATE_END);

        assertEquals(30, dataPoints.size());
        var vo2Max = dataPoints.getFirst().getDailyVo2Max();
        assertEquals(30, vo2Max.getDate().getDay());
        assertEquals("49.14", vo2Max.getVo2Max().setScale(2, RoundingMode.HALF_UP).toPlainString());
        assertEquals(CardioFitnessLevelEnum.VERY_GOOD, vo2Max.getCardioFitnessLevel());
    }

    @Test
    void getReconcile_sleep_success() {
        // the sample is the first page only, it contains a nextPageToken
        String path = BASE_PATH + "/dataTypes/sleep/dataPoints:reconcile";
        String filter = "sleep.interval.civil_end_time >= \"2026-09-01\" AND sleep.interval.civil_end_time < \"2026-10-01\"";
        wiremock.register(authorized(get(urlPathEqualTo(path)))
                .withQueryParam("filter", equalTo(filter))
                .withQueryParam("pageToken", absent())
                .willReturn(okJson(sample("sleep.response.json"))));
        wiremock.register(authorized(get(urlPathEqualTo(path)))
                .withQueryParam("filter", equalTo(filter))
                .withQueryParam("pageToken", equalTo("chcKCYIBBgjU2bnVBgoKEMjQseKj75-RMQ"))
                .willReturn(okJson("{}")));

        var dataPoints = testee.getReconcile(SESSION, "sleep", "sleep.interval.civil_end_time", DATE_BEG, DATE_END);

        assertEquals(13, dataPoints.size());
        wiremock.verifyThat(2, getRequestedFor(urlPathEqualTo(path)));
        var interval = dataPoints.getFirst().getSleep().getInterval();
        assertEquals("2026-09-29T23:23:00Z", interval.getStartTime());
        assertEquals("2026-09-30T09:40:00Z", interval.getEndTime());
        assertEquals("7200s", interval.getEndUtcOffset());
    }

    @Test
    void getReconcile_multiplePages_allCollected() {
        String path = BASE_PATH + "/dataTypes/daily-resting-heart-rate/dataPoints:reconcile";
        wiremock.register(authorized(get(urlPathEqualTo(path)))
                .withQueryParam("pageToken", absent())
                .willReturn(okJson("""
                        { "dataPoints": [ { "dailyRestingHeartRate": { "beatsPerMinute": "60" } } ], "nextPageToken": "p2" }
                        """)));
        wiremock.register(authorized(get(urlPathEqualTo(path)))
                .withQueryParam("pageToken", equalTo("p2"))
                .willReturn(okJson(sample("heartrate.response.json"))));

        var dataPoints = testee.getReconcile(SESSION, "daily-resting-heart-rate", "daily_resting_heart_rate.date", DATE_BEG, DATE_END);

        assertEquals(1 + 30, dataPoints.size());
        assertEquals("60", dataPoints.getFirst().getDailyRestingHeartRate().getBeatsPerMinute());
        wiremock.verifyThat(2, getRequestedFor(urlPathEqualTo(path)));
    }

    @Test
    void getList_activity_success() {
        // the sample is the first page only, it contains a nextPageToken
        String path = BASE_PATH + "/dataTypes/exercise/dataPoints";
        String filter = "exercise.interval.civil_start_time >= \"2026-09-01\" AND exercise.interval.civil_start_time < \"2026-10-01\"";
        wiremock.register(authorized(get(urlPathEqualTo(path)))
                .withQueryParam("filter", equalTo(filter))
                .withQueryParam("pageToken", absent())
                .willReturn(okJson(sample("activity.response.json"))));
        wiremock.register(authorized(get(urlPathEqualTo(path)))
                .withQueryParam("filter", equalTo(filter))
                .withQueryParam("pageToken", equalTo("25"))
                .willReturn(okJson("{}")));

        var dataPoints = testee.getList(SESSION, "exercise", "exercise.interval.civil_start_time", DATE_BEG, DATE_END);

        assertEquals(25, dataPoints.size());
        wiremock.verifyThat(2, getRequestedFor(urlPathEqualTo(path)));
        var first = dataPoints.getFirst();
        assertEquals("users/4981657191938844637/dataTypes/exercise/dataPoints/3163336676098067440", first.getName());
        assertEquals("Charge 6", first.getDataSource().getDevice().getDisplayName());
        assertEquals("Gehen", first.getExercise().getDisplayName());
        assertEquals(ExerciseTypeEnum.WALKING, first.getExercise().getExerciseType());
        assertEquals("2026-09-30T14:16:52Z", first.getExercise().getInterval().getStartTime());
    }

    @Test
    void getList_empty_emptyList() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/exercise/dataPoints")))
                .willReturn(okJson("{}")));

        var dataPoints = testee.getList(SESSION, "exercise", "exercise.interval.civil_start_time", DATE_BEG, DATE_END);

        assertTrue(dataPoints.isEmpty());
    }

    @Test
    void getExportExerciseTcx_valid_success() {
        wiremock.register(authorized(get(urlPathEqualTo(BASE_PATH + "/dataTypes/exercise/dataPoints/3163336676098067440:exportExerciseTcx")))
                .willReturn(okJson(sample("tcx.response.json"))));

        String tcx = testee.getExportExerciseTcx(SESSION, 3163336676098067440L);

        assertTrue(tcx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
        assertTrue(tcx.contains("<TrainingCenterDatabase"));
    }

    private static MappingBuilder authorized(MappingBuilder mapping) {
        return mapping.withHeader("Authorization", equalTo("Bearer test-token"));
    }

    private static String sample(String name) {
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
