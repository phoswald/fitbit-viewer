package com.github.phoswald.fitbit.viewer.tcx;

public record GeoPoint(double latitude, double longitude) {

    public double[] toVector() {
        return new double[] { latitude, longitude };
    }
}
