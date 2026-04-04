package com.github.phoswald.fitbit.viewer.activities;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityViewModel(
        LocalDate begDate,
        int limit,
        List<ActivityEntry> entries,
        String errorMessage,
        ZonedDateTime now
) {
    @RecordBuilder
    record ActivityEntry(
            Long logId,
            String activityName,
            OffsetDateTime startTime,
            long durationMinutes,
            Integer calories,
            Integer steps,
            Double distance,
            String distanceUnit,
            Integer averageHeartRate,
            String logType
    ) { }

    static ActivityViewModel create(
            LocalDate begDate,
            int limit,
            List<ActivityApiClient.ActivityEntry> apiEntries) {
        return new ActivityViewModelBuilder()
                .begDate(begDate)
                .limit(limit)
                .entries(apiEntries == null ? null : apiEntries.stream().map(ActivityViewModel::createEntry).toList())
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityViewModel createError(String errorMessage) {
        return new ActivityViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    private static ActivityEntry createEntry(ActivityApiClient.ActivityEntry e) {
        return new ActivityEntryBuilder()
                .logId(e.logId())
                .activityName(e.activityName())
                .startTime(e.startTime() != null ? OffsetDateTime.parse(e.startTime()) : null)
                .durationMinutes(e.duration() != null ? e.duration() / 60_000 : 0)
                .calories(e.calories())
                .steps(e.steps())
                .distance(e.distance())
                .distanceUnit(e.distanceUnit())
                .averageHeartRate(e.averageHeartRate())
                .logType(e.logType())
                .build();
    }
}
