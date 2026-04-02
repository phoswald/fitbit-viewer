package com.github.phoswald.fitbit.viewer.steps;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record StepsViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<StepsEntry> steps,
        String errorMessage,
        ZonedDateTime now
) {
    record StepsEntry(LocalDate date, Integer stepCount) { }

    static StepsViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<StepsApiClient.StepsEntry> steps) {
        return new StepsViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .steps(steps == null ? null : steps.stream().map(StepsViewModel::createEntry).toList())
                .now(ZonedDateTime.now())
                .build();
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    static StepsEntry createEntry(StepsApiClient.StepsEntry entry) {
        return new StepsEntry(LocalDate.parse(entry.dateTime()), Integer.parseInt(entry.value()));
    }
}
