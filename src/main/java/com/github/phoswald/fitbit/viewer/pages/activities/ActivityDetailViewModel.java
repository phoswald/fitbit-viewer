package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.tcx.GeoPoint;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityDetailViewModel(
        Long logId,
        LocalDate date,
        ActivityEntity activity,
        ActivityDetailEntry entry,
        String errorMessage,
        ZonedDateTime now
) {
    @RecordBuilder
    record ActivityDetailEntry( // TODO remove
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
            List<GeoPoint> track
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

    static ActivityDetailViewModel create(Long logId, ActivityEntity activity, List<GeoPoint> track) {
        return new ActivityDetailViewModelBuilder()
                .logId(logId)
                .activity(activity)
                .entry(createEntry(activity, track))
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityDetailViewModel createError(String errorMessage) {
        return new ActivityDetailViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    private static ActivityDetailEntry createEntry(ActivityEntity activity, List<GeoPoint> track) {
        return new ActivityDetailEntryBuilder()
                .logId(activity.getLogId())
                .logType(activity.getLogType())
                .activityName(activity.getActivityName())
                .startTime(activity.getBegDateTime())
                .durationMinutes(activity.getDurationMinutes())
//              .activeDurationMinutes(e.activeDuration() == null ? null : e.activeDuration().intValue() / 60_000)
                .calories(activity.getCalories())
                .steps(activity.getSteps())
                .distance(activity.getDistance())
                .distanceUnit(activity.getDistanceUnit())
                .averageHeartRate(activity.getAverageHeartRate())
//                .heartRateZones(e.heartRateZones() == null ? null : e.heartRateZones().stream().map(ActivityDetailViewModel::createZone).toList())
//                .activityLevels(e.activityLevel() == null ? null : e.activityLevel().stream().map(ActivityDetailViewModel::createLevel).toList())
//                .pace(e.pace())
//                .speed(e.speed())
//                .elevationGain(e.elevationGain())
//                .floors(e.floors())
//                .source(e.source() == null ? null : createSource(e.source()))
                .track(track)
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
