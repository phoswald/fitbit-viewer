package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;
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
            OffsetDateTime begDateTime,
            OffsetDateTime endDateTime,
            Long durationMinutes,
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
                .begDateTime(e.startTime() == null ? null : OffsetDateTime.parse(e.startTime()))
                .endDateTime(e.startTime() == null || e.duration() == null ? null : OffsetDateTime.parse(e.startTime()).plusSeconds(e.duration() / 1000))
                .durationMinutes(e.duration() == null ? null : (e.duration() / 60_000))
                .calories(e.calories())
                .steps(e.steps())
                .distance(e.distance())
                .distanceUnit(e.distanceUnit())
                .averageHeartRate(e.averageHeartRate())
                .logType(e.logType())
                .build();
    }
}
