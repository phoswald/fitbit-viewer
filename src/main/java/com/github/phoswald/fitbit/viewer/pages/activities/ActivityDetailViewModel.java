package com.github.phoswald.fitbit.viewer.pages.activities;

import static com.github.phoswald.fitbit.viewer.TimedValue.add;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.github.phoswald.fitbit.viewer.TimedValue;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.repository.TcxEntity;
import com.github.phoswald.fitbit.viewer.tcx.GeoLap;
import com.github.phoswald.fitbit.viewer.tcx.GeoPoint;
import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;
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
        TcxEntity tcx,
        List<String> labels,
        List<String> allLabels,
        boolean editLabels,
        List<GeoPoint> track,
        List<GeoLap> laps,
        List<TimedValue> altitudes,
        List<TimedValue> heartRates,
        String userId,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityDetailViewModel create(Long logId, ActivityEntity activity, Optional<TcxEntity> tcx, List<String> allLabels, boolean editLabels, String userId) {
        var tcxDb = tcx.flatMap(TcxEntity::getTcxDatabase);
        return new ActivityDetailViewModelBuilder()
                .logId(logId)
                .activity(activity)
                .tcx(tcx.orElse(null))
                .labels(activity.getLabels())
                .allLabels(allLabels)
                .editLabels(editLabels)
                .track(tcxDb.map(TcxDatabase::collectGeoPoints).orElse(List.of()))
                .laps(tcxDb.map(TcxDatabase::collectGeoLaps).orElse(List.of()))
                .altitudes(tcxDb.map(TcxDatabase::collectAltitudes).orElse(List.of()))
                .heartRates(tcxDb.map(TcxDatabase::collectHeartRates).orElse(List.of()))
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

    public Chart altitudesChart() {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .datasets(List.of(Chart.createDatasetOfTimeSeries("Altitude (m)", 0, add(altitudes, tcx.getAltitudeCorrection()))))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .x(Chart.createTimeAxis())
                                .build())
                        .build())
                .build();
    }

    public Chart heartRatesChart() {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .datasets(List.of(Chart.createDatasetOfTimeSeries("Heart Rate (bpm)", 0, heartRates)))
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
