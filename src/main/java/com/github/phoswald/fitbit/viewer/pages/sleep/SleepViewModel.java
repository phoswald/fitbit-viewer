package com.github.phoswald.fitbit.viewer.pages.sleep;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.SleepEntity;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record SleepViewModel(
        DateRangeViewModel dateRange,
        Collection<SleepEntity> sleeps,
        String errorMessage,
        ZonedDateTime now
) {

    static SleepViewModel create(DateRangeViewModel dateRange, Collection<SleepEntity> sleeps) {
        return new SleepViewModelBuilder()
                .dateRange(dateRange)
                .sleeps(sleeps)
                .now(ZonedDateTime.now())
                .build();
    }

    static SleepViewModel createError(String errorMessage) {
        return new SleepViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public Chart classicSleepChart() {
        return new ChartBuilder()
                .type("bar")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(sleeps, SleepEntity::getDate))
                        .datasets(List.of(
                                Chart.createDataset("Asleep",   "classic", sleeps, SleepEntity::getMinutesAsleep),
                                Chart.createDataset("Restless", "classic", sleeps, SleepEntity::getMinutesRestless),
                                Chart.createDataset("Awake",    "classic", sleeps, SleepEntity::getMinutesAwake)))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .y(new ChartOptionsAxisBuilder()
                                        .beginAtZero(Boolean.TRUE)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public Chart stagesSleepChart() {
        return new ChartBuilder()
                .type("bar")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(sleeps, SleepEntity::getDate))
                        .datasets(List.of(
                                Chart.createDataset("Deep",  "stages", sleeps, SleepEntity::getMinutesDeep),
                                Chart.createDataset("Light", "stages", sleeps, SleepEntity::getMinutesLight),
                                Chart.createDataset("REM",   "stages", sleeps, SleepEntity::getMinutesRem),
                                Chart.createDataset("Wake",  "stages", sleeps, SleepEntity::getMinutesWake)))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .y(new ChartOptionsAxisBuilder()
                                        .beginAtZero(Boolean.TRUE)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
