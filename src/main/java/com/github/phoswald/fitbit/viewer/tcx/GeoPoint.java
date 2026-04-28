package com.github.phoswald.fitbit.viewer.tcx;

public record GeoPoint(double latitude, double longitude) {

    public Double[] toVector() {
        return new Double[] { latitude, longitude };
    }
}
