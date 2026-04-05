package com.github.phoswald.fitbit.viewer.activities;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityDetailViewModel(
        Long logId,
        LocalDate date,
        ActivityDetailEntry entry,
        String errorMessage,
        ZonedDateTime now
) {
    @RecordBuilder
    record ActivityDetailEntry(
            Long logId,
            String activityName,
            OffsetDateTime startTime,
            Integer durationMinutes,
            Integer activeDurationMinutes,
            Integer calories,
            Integer steps,
            Double distance,
            String distanceUnit,
            Integer averageHeartRate,
            String logType,
            List<HeartRateZoneEntry> heartRateZones,
            List<ActivityLevelEntry> activityLevels,
            Double pace,
            Double speed,
            Double elevationGain,
            Integer floors,
            ActivitySourceEntry source,
            List<TrackPoint> trackPoints
    ) { }

    @RecordBuilder
    record HeartRateZoneEntry(
            String name,
            Integer minutes,
            Double caloriesOut,
            Integer minBpm,
            Integer maxBpm
    ) { }

    @RecordBuilder
    record ActivityLevelEntry(
            String name,
            Integer minutes
    ) { }

    @RecordBuilder
    record ActivitySourceEntry(
            String name,
            String type,
            String url
    ) { }

    static ActivityDetailViewModel create(Long logId, LocalDate date, ActivityApiClient.ActivityEntry apiEntry, List<TrackPoint> trackPoints) {
        return new ActivityDetailViewModelBuilder()
                .logId(logId)
                .date(date)
                .entry(createEntry(apiEntry, trackPoints))
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityDetailViewModel createError(String errorMessage) {
        return new ActivityDetailViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    private static ActivityDetailEntry createEntry(ActivityApiClient.ActivityEntry e, List<TrackPoint> trackPoints) {
        return new ActivityDetailEntryBuilder()
                .logId(e.logId())
                .activityName(e.activityName())
                .startTime(e.startTime() == null ? null : OffsetDateTime.parse(e.startTime()))
                .durationMinutes(e.duration() == null ? null : e.duration().intValue() / 60_000)
                .activeDurationMinutes(e.activeDuration() == null ? null : e.activeDuration().intValue() / 60_000)
                .calories(e.calories())
                .steps(e.steps())
                .distance(e.distance())
                .distanceUnit(e.distanceUnit())
                .averageHeartRate(e.averageHeartRate())
                .logType(e.logType())
                .heartRateZones(e.heartRateZones() == null ? null : e.heartRateZones().stream().map(ActivityDetailViewModel::createZone).toList())
                .activityLevels(e.activityLevel() == null ? null : e.activityLevel().stream().map(ActivityDetailViewModel::createLevel).toList())
                .pace(e.pace())
                .speed(e.speed())
                .elevationGain(e.elevationGain())
                .floors(e.floors())
                .source(e.source() == null ? null : createSource(e.source()))
                .trackPoints(trackPoints)
                .build();
    }

    private static HeartRateZoneEntry createZone(ActivityApiClient.HeartRateZone z) {
        return new HeartRateZoneEntryBuilder()
                .name(z.name())
                .minutes(z.minutes())
                .caloriesOut(z.caloriesOut())
                .minBpm(z.min())
                .maxBpm(z.max())
                .build();
    }

    private static ActivityLevelEntry createLevel(ActivityApiClient.ActivityLevel l) {
        return new ActivityLevelEntryBuilder()
                .name(l.name())
                .minutes(l.minutes())
                .build();
    }

    private static ActivitySourceEntry createSource(ActivityApiClient.ActivitySource s) {
        return new ActivitySourceEntryBuilder()
                .name(s.name())
                .type(s.type())
                .url(s.url())
                .build();
    }
}
