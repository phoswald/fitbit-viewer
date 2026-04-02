package com.github.phoswald.fitbit.viewer.steps;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record StepsViewModel(
        String begDate,
        String endDate,
        List<StepsEntry> steps,
        String errorMessage,
        String now
) {
    record StepsEntry(String date, Integer stepCount) { }

    static StepsViewModel create(
            String begDate,
            String endDate,
            List<StepsApiClient.StepsEntry> steps) {
        return new StepsViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .steps(steps == null ? null : steps.stream().map(StepsViewModel::createEntry).toList())
                .now(getNow())
                .build();
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModelBuilder()
                .errorMessage(errorMessage)
                .now(getNow())
                .build();
    }

    static StepsEntry createEntry(StepsApiClient.StepsEntry entry) {
        return new StepsEntry(entry.dateTime(), Integer.parseInt(entry.value()));
    }

    private static String getNow() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
