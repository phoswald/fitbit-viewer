package com.github.phoswald.fitbit.viewer.steps;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

record StepsViewModel(
        String errorMessage,
        String startDate,
        String endDate,
        List<StepsEntry> steps,
        String now /* = ; */
) {
    record StepsEntry(String date, Integer steps) { }

    static StepsViewModel create(
            String startDate,
            String endDate,
            StepsApiClient.StepsResponse response) {
        return new StepsViewModel(
                null,
                startDate,
                endDate,
                response == null ? null : response.activitiesSteps().stream().map(e -> createEntry(e)).toList(),
                getNow());
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModel(
                errorMessage,
                null,
                null,
                null,
                getNow());
    }

    static StepsEntry createEntry(StepsApiClient.StepsEntry entry) {
        return new StepsEntry(entry.dateTime(), Integer.parseInt(entry.value()));
    }

    private static String getNow() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
