package com.github.phoswald.fitbit.viewer.pages.steps;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record StepsViewModel(
        DateRangeViewModel dateRange,
        Collection<StepsEntity> steps,
        String errorMessage,
        ZonedDateTime now
) {

    static StepsViewModel create(
            DateRangeViewModel dateRange,
            Collection<StepsEntity> steps) {
        return new StepsViewModelBuilder()
                .dateRange(dateRange)
                .steps(steps)
                .now(ZonedDateTime.now())
                .build();
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> stepDates() {
        return steps.stream().map(StepsEntity::getDate).toList();
    }

    public List<Integer> stepCounts() {
        return steps.stream().map(StepsEntity::getStepCount).toList();
    }
}
