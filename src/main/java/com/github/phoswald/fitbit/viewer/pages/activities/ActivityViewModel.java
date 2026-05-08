package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityViewModel(
        DateRangeViewModel dateRange,
        String label,
        boolean excludeAuto,
        boolean excludeLowCal,
        Collection<ActivityEntity> activities,
        List<String> allLabels,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityViewModel create(
            DateRangeViewModel dateRange,
            String label,
            boolean excludeAuto,
            boolean excludeLowCal,
            Collection<ActivityEntity> activities,
            List<String> allLabels) {
        return new ActivityViewModelBuilder()
                .dateRange(dateRange)
                .label(label)
                .excludeAuto(excludeAuto)
                .excludeLowCal(excludeLowCal)
                .activities(activities.stream()
                        .filter(a -> label == null || a.getLabels().contains(label)
                                || (Objects.equals(label, "(none)") && a.getLabels().isEmpty()))
                        .filter(a -> !excludeAuto || !a.isAutoDetected())
                        .filter(a -> !excludeLowCal || !a.isLowCal())
                        .toList())
                .allLabels(allLabels)
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
