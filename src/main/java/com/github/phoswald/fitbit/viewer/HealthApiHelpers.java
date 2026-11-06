package com.github.phoswald.fitbit.viewer;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseInteger;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseOffsetDateTime;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.round;

import java.time.LocalDate;

import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRange;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeEnd;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeEndDate;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeStart;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeStartDate;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInner;
import com.google.api.health.v4.model.ListDataPointsResponse;
import com.google.api.health.v4.model.ListDataPointsResponseDataPointsInner;
import com.google.api.health.v4.model.ProfileMembershipStartDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyRestingHeartRateDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerDailyVo2MaxDate;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleep;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInnerSleepSummaryStagesSummaryInner;

public class HealthApiHelpers {

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
        if(sleep.getInterval() == null) {
            return null;
        }
        return ValueHelpers.dateOf(parseOffsetDateTime(sleep.getInterval().getEndTime(), sleep.getInterval().getEndUtcOffset()));
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

    public static Long logIdOf(ListDataPointsResponseDataPointsInner dataPoint) {
        if(dataPoint == null || dataPoint.getName() == null) {
            return null;
        }
        String dataPointName = dataPoint.getName();
        // format: users/{user}/dataTypes/{dataType}/dataPoints/{dataPointName}
        return Long.parseLong(dataPointName.substring(dataPointName.lastIndexOf('/') + 1));
    }
}
