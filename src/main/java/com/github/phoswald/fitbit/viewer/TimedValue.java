package com.github.phoswald.fitbit.viewer;

import java.time.Instant;
import java.util.List;

public record TimedValue(Instant timestamp, double value) {

    public double[] toVector() {
        return new double[] { timestamp.toEpochMilli(), value };
    }

    public TimedValue add(int offset) {
        return new TimedValue(timestamp, value + offset);
    }

    public static List<TimedValue> add(List<TimedValue> series, Integer offset) {
        if(offset == null || offset == 0) {
            return series;
        } else {
            return series.stream().map(value -> value.add(offset)).toList();
        }
    }
}
