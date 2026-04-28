package com.github.phoswald.fitbit.viewer.pages.azm;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesEntity;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActiveZoneMinutesViewModel(
        DateRangeViewModel dateRange,
        Collection<ActiveZoneMinutesEntity> azms,
        String errorMessage,
        ZonedDateTime now
) {

    static ActiveZoneMinutesViewModel create(
            DateRangeViewModel dateRange,
            Collection<ActiveZoneMinutesEntity> azms) {
        return new ActiveZoneMinutesViewModelBuilder()
                .dateRange(dateRange)
                .azms(azms)
                .now(ZonedDateTime.now())
                .build();
    }

    static ActiveZoneMinutesViewModel createError(String errorMessage) {
        return new ActiveZoneMinutesViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public Chart azmChart() {
        return new ChartBuilder()
                .type("bar")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(azms, ActiveZoneMinutesEntity::getDate))
                        .datasets(List.of(
                                Chart.createDataset("Fat Burn", "azm", azms, ActiveZoneMinutesEntity::getFatBurnAzm),
                                Chart.createDataset("Cardio", "azm", azms, ActiveZoneMinutesEntity::getCardioAzm),
                                Chart.createDataset("Peak", "azm", azms, ActiveZoneMinutesEntity::getPeakAzm)))
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
