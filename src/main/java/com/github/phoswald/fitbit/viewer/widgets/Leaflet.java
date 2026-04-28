package com.github.phoswald.fitbit.viewer.widgets;

import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Leaflet(
        String titleLayerUrlTemplate,
        LeafletTitleLayerOptions titleLayerOptions,
        List<Double[]> polyLineCoords,
        LeafletPolyLineOptions polyLineOptions
) {

    public static Leaflet createWithPolyLine(List<Double[]> polyLineCoords) {
        return new LeafletBuilder()
                .titleLayerUrlTemplate("https://tile.openstreetmap.org/{z}/{x}/{y}.png")
                .titleLayerOptions(new LeafletTitleLayerOptionsBuilder()
                        .maxZoom(19)
                        .attribution("&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors")
                        .build())
                .polyLineCoords(polyLineCoords)
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
