package com.github.phoswald.fitbit.viewer;

import java.time.Instant;

public record TimedValue(Instant timestamp, double value) {

    public double[] toVector() {
        return new double[] { timestamp.toEpochMilli(), value };
    }
}
