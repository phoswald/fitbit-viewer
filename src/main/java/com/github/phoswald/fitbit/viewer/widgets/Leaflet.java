package com.github.phoswald.fitbit.viewer.widgets;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Leaflet(
        String titleLayerUrlTemplate,
        LeafletTitleLayerOptions titleLayerOptions,
        List<double[]> polyLineCoords,
        LeafletPolyLineOptions polyLineOptions
) {

    public static <T> Leaflet createWithPolyLine(Collection<T> data, Function<T, Double> latitude, Function<T, Double> longitude) {
        return createWithPolyLine(data.stream().map(e -> new double[] { latitude.apply(e), longitude.apply(e) }).toList());
    }

    public static Leaflet createWithPolyLine(List<double[]> points) {
        return new LeafletBuilder()
                .titleLayerUrlTemplate("https://tile.openstreetmap.org/{z}/{x}/{y}.png")
                .titleLayerOptions(new LeafletTitleLayerOptionsBuilder()
                        .maxZoom(19)
                        .attribution("&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors")
                        .build())
                .polyLineCoords(points)
                .polyLineOptions(new LeafletPolyLineOptionsBuilder()
                        .color("#6d4aaa")
                        .weight(3)
                        .build())
                .build();
    }

    @RecordBuilder
    public record LeafletTitleLayerOptions(Integer maxZoom, String attribution) { }

    @RecordBuilder
    public record LeafletPolyLineOptions(String color, Integer weight) { }
}
