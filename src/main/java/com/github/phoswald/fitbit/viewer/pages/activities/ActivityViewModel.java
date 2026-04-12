package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.ZonedDateTime;
import java.util.Collection;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityViewModel(
        DateRangeViewModel dateRange,
        boolean excludeAuto,
        boolean excludeLowCal,
        Collection<ActivityEntity> activities,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityViewModel create(
            DateRangeViewModel dateRange,
            boolean excludeAuto,
            boolean excludeLowCal,
            Collection<ActivityEntity> activities) {
        return new ActivityViewModelBuilder()
                .dateRange(dateRange)
                .excludeAuto(excludeAuto)
                .excludeLowCal(excludeLowCal)
                .activities(activities.stream()
                        .filter(a -> !excludeAuto || !a.isAutoDetected())
                        .filter(a -> !excludeLowCal || !a.isLowCal())
                        .toList())
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityViewModel createError(String errorMessage) {
        return new ActivityViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }
}
