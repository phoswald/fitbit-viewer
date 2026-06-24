package com.github.phoswald.fitbit.viewer.pages.activities;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.tcx.GeoLap;
import com.github.phoswald.fitbit.viewer.tcx.GeoPoint;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.fitbit.viewer.widgets.Leaflet;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityDetailViewModel(
        Long logId,
        LocalDate date,
        ActivityEntity activity,
        List<String> labels,
        List<String> allLabels,
        List<GeoPoint> track,
        List<GeoLap> laps,
        boolean editLabels,
        String userId,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityDetailViewModel create(Long logId, ActivityEntity activity, List<String> allLabels, List<GeoPoint> track, List<GeoLap> laps, boolean editLabels, String userId) {
        return new ActivityDetailViewModelBuilder()
                .logId(logId)
                .activity(activity)
                .labels(activity.getLabels())
                .allLabels(allLabels)
                .track(track)
                .laps(laps)
                .editLabels(editLabels)
                .userId(userId)
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

    public Chart lapsChart() {
        return new ChartBuilder()
                .type("bar")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(laps, GeoLap::number))
                        .datasets(List.of(Chart.createDataset("Pace (min/km)", laps, divideBy(GeoLap::pace, 60))))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .y(new ChartOptionsAxisBuilder()
                                        .beginAtZero(true)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
