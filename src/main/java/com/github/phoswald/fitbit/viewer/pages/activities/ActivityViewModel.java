package com.github.phoswald.fitbit.viewer.pages.activities;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityViewModel(
        DateRangeViewModel dateRange,
        String label,
        boolean excludeAuto,
        boolean excludeLowCal,
        Collection<ActivityEntity> activities,
        List<String> allLabels,
        String userId,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityViewModel create(
            DateRangeViewModel dateRange,
            String label,
            boolean excludeAuto,
            boolean excludeLowCal,
            Collection<ActivityEntity> activities,
            List<String> allLabels,
            String userId) {
        return new ActivityViewModelBuilder()
                .dateRange(dateRange)
                .label(label)
                .excludeAuto(excludeAuto)
                .excludeLowCal(excludeLowCal)
                .activities(activities.stream()
                        .filter(a -> label == null || a.getLabels().contains(label)
                                || (Objects.equals(label, "(none)") && a.getLabels().isEmpty()))
                        .filter(a -> !excludeAuto || !a.isAutoDetected())
                        .filter(a -> !excludeLowCal || !a.isLowCal())
                        .toList())
                .allLabels(allLabels)
                .userId(userId)
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityViewModel createError(String errorMessage) {
        return new ActivityViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public Chart activitiesChart() {
        return lineChart("Duration (min)", activities, ActivityEntity::getDurationMinutes);
    }

    public Chart activitiesStepsChart() {
        return lineChart("Steps", activities, ActivityEntity::getSteps);
    }

    public Chart activitiesDistanceChart() {
        return lineChart("Distance (km)", activities, ActivityEntity::getDistance);
    }

    public Chart activitiesPaceChart() {
        return lineChart("Pace (min/km)", activities, divideBy(ActivityEntity::getPace, 60));
    }

    public Chart activitiesHeartRateChart() {
        return lineChart("Heart Rate (avg. bpm)", activities, ActivityEntity::getAverageHeartRate);
    }

    private static Chart lineChart(String label, Collection<ActivityEntity> data, Function<ActivityEntity, ? extends Number> field) {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(data, a -> a.getBegDateTime().toLocalDate()))
                        .datasets(List.of(Chart.createDataset(label, data, field)))
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
