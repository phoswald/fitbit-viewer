package com.github.phoswald.fitbit.viewer.widgets;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Chart(String type, ChartData data, ChartOptions options) {

    public static <T> List<String> createLabels(Collection<T> data, Function<T, Object> field) {
        return data.stream().map(field).map(Object::toString).toList();
    }

    public static <T> ChartDataset createDataset(String label, Collection<T> data, Function<T, Integer> field) {
        return createDataset(label, null, data, field);
    }

    public static <T> ChartDataset createDataset(String label, String stack, Collection<T> data, Function<T, Integer> field) {
        return new ChartDatasetBuilder()
                .label(label)
                .stack(stack)
                .data(data.stream().map(field).toList())
                .build();
    }

    @RecordBuilder
    public record ChartData(List<String> labels, List<ChartDataset> datasets) { }

    @RecordBuilder
    public record ChartDataset(String label, String stack, List<Integer> data) { }

    @RecordBuilder
    public record ChartOptions(ChartOptionsScales scales) { }

    @RecordBuilder
    public record ChartOptionsScales(ChartOptionsAxis y) { }

    @RecordBuilder
    public record ChartOptionsAxis(Boolean beginAtZero, String grace) { }
}
