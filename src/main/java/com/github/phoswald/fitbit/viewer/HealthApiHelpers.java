package com.github.phoswald.fitbit.viewer;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseInteger;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.round;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.google.api.health.v4.api.DefaultApi;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRange;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeEnd;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeEndDate;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeStart;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeStartDate;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInner;
import com.google.api.health.v4.model.ListDataPointsResponse;
import com.google.api.health.v4.model.ListDataPointsResponseDataPointsInner;
import com.google.api.health.v4.model.ProfileMembershipStartDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInner;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRateDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyVo2MaxDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleep;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepInterval;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner;

public class HealthApiHelpers {

    /**
     * Queries reconciled data points, which must fit on a single page, as paging is not implemented.
     *
     * @throws IllegalStateException if the response has further pages
     */
    public static List<ReconcileDataPointsResponseDataPointsInner> reconcile(
            DefaultApi client, String authorization, String parent, String filter) {
        var response = client.healthUsersDataTypesDataPointsReconcile(
                authorization, parent, null, null, null, encodeQueryParam(filter));
        if(response.getNextPageToken() != null && !response.getNextPageToken().isEmpty()) {
            throw new IllegalStateException("Too many data points for a single page: parent=" + parent + ", filter=" + filter);
        }
        return response.getDataPoints() == null ? List.of() : response.getDataPoints();
    }

    /**
     * Creates a civil date filter for the inclusive range from dateBeg to dateEnd.
     */
    public static String dateFilter(String field, LocalDate dateBeg, LocalDate dateEnd) {
        return field + " >= \"" + dateBeg + "\" AND " + field + " < \"" + dateEnd.plusDays(1) + "\"";
    }

    /**
     * Encodes a query parameter, as the Encoded annotation on the generated API (see application.properties)
     * turns off encoding by the client.
     */
    static String encodeQueryParam(String value) {
        return URLEncoder.encode(value, UTF_8).replace("+", "%20");
    }

    public static DailyRollUpDataPointsRequestRange dateRange(LocalDate dateBeg, LocalDate dateEnd) {
        return new DailyRollUpDataPointsRequestRange()
                .start(new DailyRollUpDataPointsRequestRangeStart().date(startDate(dateBeg)))
                .end(new DailyRollUpDataPointsRequestRangeEnd().date(endDate(dateEnd.plusDays(1))));
    }

    private static DailyRollUpDataPointsRequestRangeStartDate startDate(LocalDate date) {
        return new DailyRollUpDataPointsRequestRangeStartDate()
                .year(date.getYear())
                .month(date.getMonthValue())
                .day(date.getDayOfMonth());
    }

    private static DailyRollUpDataPointsRequestRangeEndDate endDate(LocalDate date) {
        return new DailyRollUpDataPointsRequestRangeEndDate()
                .year(date.getYear())
                .month(date.getMonthValue())
                .day(date.getDayOfMonth());
    }

    public static LocalDate dateOf(DailyRollUpDataPointsResponseRollupDataPointsInner entry) {
        if(entry.getCivilStartTime() == null || entry.getCivilStartTime().getDate() == null) {
            return null;
        }
        var date = entry.getCivilStartTime().getDate();
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    public static LocalDate dateOf(ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRateDate date) {
        return date == null ? null : LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    public static LocalDate dateOf(ReconcileDataPointsResponseDataPointsInnerDailyVo2MaxDate date) {
        return date == null ? null : LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    /**
     * Returns the local date on which the sleep session ended. Replaces dateOfSleep of the Fitbit API.
     * Calculated from endTime and endUtcOffset, as the API does not return interval.civilEndTime.
     */
    public static LocalDate dateOf(ReconcileDataPointsResponseDataPointsInnerSleep sleep) {
        ReconcileDataPointsResponseDataPointsInnerSleepInterval interval = sleep.getInterval();
        if(interval == null || interval.getEndTime() == null || interval.getEndUtcOffset() == null) {
            return null;
        }
        Instant endTime = Instant.parse(interval.getEndTime());
        Duration endUtcOffset = Duration.parse("PT" + interval.getEndUtcOffset());
        return LocalDate.ofInstant(endTime.plus(endUtcOffset), ZoneOffset.UTC);
    }

    public static Integer stageMinutesOf(
            ReconcileDataPointsResponseDataPointsInnerSleep sleep,
            ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner.TypeEnum type) {
        if(sleep.getSummary() == null || sleep.getSummary().getStagesSummary() == null) {
            return null;
        }
        return sleep.getSummary().getStagesSummary().stream()
                .filter(stage -> stage.getType() == type)
                .map(stage -> parseInteger(stage.getMinutes()))
                .findFirst()
                .orElse(null);
    }

    public static Integer stepCountOf(DailyRollUpDataPointsResponseRollupDataPointsInner entry) {
        if(entry.getSteps() == null || entry.getSteps().getCountSum() == null) {
            return null;
        }
        return Integer.parseInt(entry.getSteps().getCountSum());
    }

    public static String dateOf(ProfileMembershipStartDate date) {
        if(date == null) {
            return null;
        }
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay()).toString();
    }

    public static Integer heightCmOf(ListDataPointsResponse response) {
        var entry = firstOf(response);
        if(entry == null || entry.getHeight() == null || entry.getHeight().getHeightMillimeters() == null) {
            return null;
        }
        return round(divideBy(parseInteger(entry.getHeight().getHeightMillimeters()), 10));
    }

    public static Integer weightKgOf(ListDataPointsResponse response) {
        var entry = firstOf(response);
        if(entry == null || entry.getWeight() == null || entry.getWeight().getWeightGrams() == null) {
            return null;
        }
        return round(divideBy(entry.getWeight().getWeightGrams().doubleValue(), 1000));
    }

    public static Integer firstOf(Integer... values) {
        for(Integer value : values) {
             if(value != null && value != 0) {
                 return value;
             }
        }
        return null;
    }

    private static ListDataPointsResponseDataPointsInner firstOf(ListDataPointsResponse response) {
        if(response.getDataPoints() == null || response.getDataPoints().isEmpty()) {
            return null;
        }
        return response.getDataPoints().getFirst();
    }
}
