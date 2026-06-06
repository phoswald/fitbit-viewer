package com.github.phoswald.fitbit.viewer.pages.sleep;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.toDouble;

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
        String userId,
        String errorMessage,
        ZonedDateTime now
) {

    static SleepViewModel create(DateRangeViewModel dateRange, Collection<SleepEntity> sleeps, String userId) {
        return new SleepViewModelBuilder()
                .dateRange(dateRange)
                .sleeps(sleeps)
                .userId(userId)
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
                                Chart.createDataset("Asleep",   "classic", sleeps, divideBy(toDouble(SleepEntity::getMinutesAsleep),   60)),
                                Chart.createDataset("Restless", "classic", sleeps, divideBy(toDouble(SleepEntity::getMinutesRestless), 60)),
                                Chart.createDataset("Awake",    "classic", sleeps, divideBy(toDouble(SleepEntity::getMinutesAwake),    60))))
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
                                Chart.createDataset("Deep",  "stages", sleeps, divideBy(toDouble(SleepEntity::getMinutesDeep),  60)),
                                Chart.createDataset("Light", "stages", sleeps, divideBy(toDouble(SleepEntity::getMinutesLight), 60)),
                                Chart.createDataset("REM",   "stages", sleeps, divideBy(toDouble(SleepEntity::getMinutesRem),   60)),
                                Chart.createDataset("Wake",  "stages", sleeps, divideBy(toDouble(SleepEntity::getMinutesWake),  60))))
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
