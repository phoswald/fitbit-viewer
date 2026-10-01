package com.github.phoswald.fitbit.viewer.pages.activities;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.add;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.repository.TcxEntity;
import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;
import com.github.phoswald.fitbit.viewer.tcx.TcxLap;
import com.github.phoswald.fitbit.viewer.tcx.TcxTrackpoint;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisTitleBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.fitbit.viewer.widgets.Leaflet;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityDetailViewModel(
        Long logId,
        LocalDate date,
        ActivityEntity activity,
        TcxEntity tcx,
        List<String> labels,
        List<String> allLabels,
        boolean editLabels,
        List<TcxLap> laps,
        List<TcxTrackpoint> track,
        List<TcxTrackpoint> altitudes,
        List<TcxTrackpoint> heartRates,

        String userId,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityDetailViewModel create(Long logId, ActivityEntity activity, Optional<TcxEntity> tcx, List<String> allLabels, boolean editLabels, String userId) {
        var tcxDb = tcx.flatMap(TcxEntity::getTcxDatabase);
        var tcxTrack = tcxDb.map(TcxDatabase::getAllTrackpoints).orElse(List.of());
        return new ActivityDetailViewModelBuilder()
                .logId(logId)
                .activity(activity)
                .tcx(tcx.orElse(null))
                .labels(activity.getLabels())
                .allLabels(allLabels)
                .editLabels(editLabels)
                .laps(tcxDb.map(TcxDatabase::getAllLaps).orElse(List.of()))
                .track(tcxTrack)
                .altitudes(tcxTrack.stream().filter(TcxTrackpoint::hasAltitude).toList())
                .heartRates(tcxTrack.stream().filter(TcxTrackpoint::hasHeartRate).toList())
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
        return Leaflet.createWithPolyLine(track,
                TcxTrackpoint::getLatitude, TcxTrackpoint::getLongitude);
    }

    public Chart lapsChart() {
        return new ChartBuilder()
                .type("bar")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(laps, TcxLap::getNumber))
                        .datasets(List.of(Chart.createDataset("Pace (min/km)", laps, divideBy(TcxLap::getPace, 60))))
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

    public Chart altitudesChart() {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .datasets(List.of(Chart.createDatasetOfTimeSeries("Altitude (m)", 0, altitudes,
                                TcxTrackpoint::getTime, add(TcxTrackpoint::getAltitudeMeters, tcx.getAltitudeCorrection()))))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .x(Chart.createTimeAxis())
                                .build())
                        .build())
                .build();
    }

    public Chart altitudeByDistanceChart() {
        return new ChartBuilder()
                .type("scatter" /* "line" */) // or "scatter" with showLine=true
                .data(new ChartDataBuilder()
                        .datasets(List.of(Chart.createDatasetXY("Altitude (m)", 1 /* 0 */, altitudes,
                                TcxTrackpoint::getDistanceKm, add(TcxTrackpoint::getAltitudeMeters, tcx.getAltitudeCorrection()))))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .x(new ChartOptionsAxisBuilder()
                                        .type("linear")
                                        .beginAtZero(true)
                                        .title(new ChartOptionsAxisTitleBuilder()
                                                .text("Distance (km)")
                                                .display(true)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public Chart heartRatesChart() {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .datasets(List.of(Chart.createDatasetOfTimeSeries("Heart Rate (bpm)", 0, heartRates,
                                TcxTrackpoint::getTime, TcxTrackpoint::getHeartRateBpm)))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .x(Chart.createTimeAxis())
                                .y(new ChartOptionsAxisBuilder()
                                        .beginAtZero(true)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
