package com.github.phoswald.fitbit.viewer.pages.heartrate;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.HeartRateEntity;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record HeartRateViewModel(
        DateRangeViewModel dateRange,
        Collection<HeartRateEntity> heartRates,
        String errorMessage,
        ZonedDateTime now
) {

    static HeartRateViewModel create(
            DateRangeViewModel dateRange,
            Collection<HeartRateEntity> heartRates) {
        return new HeartRateViewModelBuilder()
                .dateRange(dateRange)
                .heartRates(heartRates)
                .now(ZonedDateTime.now())
                .build();
    }

    static HeartRateViewModel createError(String errorMessage) {
        return new HeartRateViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public Chart heartRatesChart() {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(heartRates, HeartRateEntity::getDate))
                        .datasets(List.of(
                                Chart.createDataset("Resting Heart Rate (bpm)", heartRates, HeartRateEntity::getRestingHeartRate)))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .y(new ChartOptionsAxisBuilder()
                                        .grace("50%")
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
