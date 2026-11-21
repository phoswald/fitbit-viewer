package com.github.phoswald.fitbit.viewer.healthapi;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseInteger;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseOffsetDateTime;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.round;

import java.time.LocalDate;

import com.github.phoswald.fitbit.viewer.ValueHelpers;
import com.google.api.health.v4.model.CivilDateTime;
import com.google.api.health.v4.model.CivilTimeInterval;
import com.google.api.health.v4.model.DailyRollupDataPoint;
import com.google.api.health.v4.model.DataPoint;
import com.google.api.health.v4.model.Date;
import com.google.api.health.v4.model.Sleep;
import com.google.api.health.v4.model.StageSummary;

public class HealthApiHelpers {

    public static CivilTimeInterval dateRange(LocalDate dateBeg, LocalDate dateEnd) {
        return new CivilTimeInterval()
                .start(dateTime(dateBeg))
                .end(dateTime(dateEnd.plusDays(1)));
    }

    private static CivilDateTime dateTime(LocalDate date) {
        return new CivilDateTime().date(new Date()
                .year(date.getYear())
                .month(date.getMonthValue())
                .day(date.getDayOfMonth()));
    }

    public static LocalDate dateOf(DailyRollupDataPoint entry) {
        if(entry.getCivilStartTime() == null || entry.getCivilStartTime().getDate() == null) {
            return null;
        }
        return dateOf(entry.getCivilStartTime().getDate());
    }

    public static LocalDate dateOf(Date date) {
        return date == null ? null : LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

    /**
     * Returns the local date on which the sleep session ended. Replaces dateOfSleep of the Fitbit API.
     * Calculated from endTime and endUtcOffset, as the API does not return interval.civilEndTime.
     */
    public static LocalDate dateOf(Sleep sleep) {
        if(sleep.getInterval() == null) {
            return null;
        }
        return ValueHelpers.dateOf(parseOffsetDateTime(sleep.getInterval().getEndTime(), sleep.getInterval().getEndUtcOffset()));
    }

    public static Integer stageMinutesOf(Sleep sleep, StageSummary.TypeEnum type) {
        if(sleep.getSummary() == null || sleep.getSummary().getStagesSummary() == null) {
            return null;
        }
        return sleep.getSummary().getStagesSummary().stream()
                .filter(stage -> stage.getType() == type)
                .map(stage -> parseInteger(stage.getMinutes()))
                .findFirst()
                .orElse(null);
    }

    public static Integer stepCountOf(DailyRollupDataPoint entry) {
        if(entry.getSteps() == null || entry.getSteps().getCountSum() == null) {
            return null;
        }
        return Integer.parseInt(entry.getSteps().getCountSum());
    }

    public static Integer heightCmOf(DataPoint entry) {
        if(entry == null || entry.getHeight() == null || entry.getHeight().getHeightMillimeters() == null) {
            return null;
        }
        return round(divideBy(parseInteger(entry.getHeight().getHeightMillimeters()), 10));
    }

    public static Integer weightKgOf(DataPoint entry) {
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

    public static Long logIdOf(DataPoint dataPoint) {
        if(dataPoint == null || dataPoint.getName() == null) {
            return null;
        }
        String dataPointName = dataPoint.getName();
        // format: users/{user}/dataTypes/{dataType}/dataPoints/{dataPointName}
        return Long.parseLong(dataPointName.substring(dataPointName.lastIndexOf('/') + 1));
    }
}
