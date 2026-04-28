package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.tcx.GeoPoint;
import com.github.phoswald.fitbit.viewer.widgets.Leaflet;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityDetailViewModel(
        Long logId,
        LocalDate date,
        ActivityEntity activity,
        List<GeoPoint> track,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityDetailViewModel create(Long logId, ActivityEntity activity, List<GeoPoint> track) {
        return new ActivityDetailViewModelBuilder()
                .logId(logId)
                .activity(activity)
                .track(track)
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityDetailViewModel createError(String errorMessage) {
        return new ActivityDetailViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public Leaflet trackMap() {
        return Leaflet.createWithPolyLine(track.stream().map(GeoPoint::toVector).toList());
    }
}
