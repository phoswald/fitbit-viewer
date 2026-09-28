package com.github.phoswald.fitbit.viewer.tcx;

import java.time.OffsetDateTime;

public record GeoAltitude(OffsetDateTime time, double altitudeMeters) {

    public double[] toVector() {
        return new double[] { time.toEpochSecond() * 1000, altitudeMeters };
    }
}
