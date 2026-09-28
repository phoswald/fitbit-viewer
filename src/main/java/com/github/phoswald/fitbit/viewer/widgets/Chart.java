package com.github.phoswald.fitbit.viewer.widgets;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.phoswald.fitbit.viewer.TimedValue;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Chart(String type, ChartData data, ChartOptions options) {

    public static <T> List<String> createLabels(Collection<T> data, Function<T, Object> accessor) {
        return data.stream().map(accessor).map(Object::toString).toList();
    }

    public static <T> ChartDataset createDataset(String label, Collection<T> data, Function<T, ? extends Number> accessor) {
        return new ChartDatasetBuilder()
                .label(label)
                .data(toNumbers(data, accessor))
                .build();
    }

    public static <T> ChartDataset createDatasetStacked(String label, String stack, Collection<T> data, Function<T, ? extends Number> accessor) {
        return new ChartDatasetBuilder()
                .label(label)
                .stack(stack)
                .data(toNumbers(data, accessor))
                .build();
    }

    public static <T> ChartDataset createDatasetOfTimeSeries(String label, Integer pointRadius, Collection<TimedValue> timeSeries) {
        return new ChartDatasetBuilder()
                .label(label)
                .data(toVectors(timeSeries, TimedValue::toVector))
                .pointRadius(pointRadius)
                .build();
    }

    private static <T> List<? extends Number> toNumbers(Collection<T> data, Function<T, ? extends Number> accessor) {
        return data.stream().map(accessor).toList();
    }

    private static <T> List<double[]> toVectors(Collection<T> data, Function<T, double[]> accessor) {
        return data.stream().map(accessor).toList();
    }

    public static ChartOptionsAxis createTimeAxis() {
        return new ChartOptionsAxisBuilder()
                .type("time") // requires data adapter JS bundle
                .time(new ChartOptionsAxisTimeBuilder()
                        .tooltipFormat("yyyy-MM-dd HH:mm:ss")
                        .displayFormats(new ChartOptionsAxisTimeDisplayFormatsBuilder()
                                .minute("yyyy-MM-dd HH:mm")
                                .build())
                        .unit("minute")
                        .build())
                .build();
    }

    @RecordBuilder
    public record ChartData(List<String> labels, List<ChartDataset> datasets) { }

    @RecordBuilder
    public record ChartDataset(
            String label,
            String stack,
            List<?> data,
            Integer pointRadius
    ) { }

    @RecordBuilder
    public record ChartOptions(ChartOptionsScales scales) { }

    @RecordBuilder
    public record ChartOptionsScales(ChartOptionsAxis x, ChartOptionsAxis y) { }

    @RecordBuilder
    public record ChartOptionsAxis(String type, Boolean beginAtZero, String grace, ChartOptionsAxisTime time) { }

    // Valid units are millisecond, second, minute, hour, day, week, month, quarter, year.
    // All of them can be used both for ChartOptionsAxisTime.unit
    // and as fields in ChartOptionsAxisTimeDisplayFormats.
    @RecordBuilder
    public record ChartOptionsAxisTime(String tooltipFormat, ChartOptionsAxisTimeDisplayFormats displayFormats, String unit) { }

    @RecordBuilder
    public record ChartOptionsAxisTimeDisplayFormats(String minute) { }
}
