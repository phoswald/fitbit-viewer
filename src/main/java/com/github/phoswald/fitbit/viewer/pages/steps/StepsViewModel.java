package com.github.phoswald.fitbit.viewer.pages.steps;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record StepsViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<StepsEntity> steps,
        String errorMessage,
        ZonedDateTime now
) {

    static StepsViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<StepsEntity> steps) {
        return new StepsViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .steps(steps)
                .now(ZonedDateTime.now())
                .build();
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModelBuilder()
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
