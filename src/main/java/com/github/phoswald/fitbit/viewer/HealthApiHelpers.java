package com.github.phoswald.fitbit.viewer;

import java.time.LocalDate;

import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRange;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeEnd;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeEndDate;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeStart;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequestRangeStartDate;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponseRollupDataPointsInner;

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

    public static Integer stepCountOf(DailyRollUpDataPointsResponseRollupDataPointsInner entry) {
        if(entry.getSteps() == null || entry.getSteps().getCountSum() == null) {
            return null;
        }
        return Integer.parseInt(entry.getSteps().getCountSum());
    }
}
