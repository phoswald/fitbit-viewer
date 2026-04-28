package com.github.phoswald.fitbit.viewer.pages.steps;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record StepsViewModel(
        DateRangeViewModel dateRange,
        Collection<StepsEntity> steps,
        String errorMessage,
        ZonedDateTime now
) {

    static StepsViewModel create(
            DateRangeViewModel dateRange,
            Collection<StepsEntity> steps) {
        return new StepsViewModelBuilder()
                .dateRange(dateRange)
                .steps(steps)
                .now(ZonedDateTime.now())
                .build();
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public Chart stepsChart() {
        return new ChartBuilder()
                .type("bar")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(steps, StepsEntity::getDate))
                        .datasets(List.of(
                                Chart.createDataset("Steps", steps, StepsEntity::getStepCount)))
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
