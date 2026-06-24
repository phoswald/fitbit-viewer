package com.github.phoswald.fitbit.viewer.tcx;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divide;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.subtract;

public record GeoLap(Integer number, Double startDistance, Double endDistance, Double timeSeconds) {

    public Double startKm() {
        return divideBy(startDistance, 1000);
    }

    public Double endKm() {
        return divideBy(endDistance, 1000);
    }

    public Double distance() {
        return subtract(endDistance, startDistance);
    }

    public Double timeMinutes() {
        return divideBy(timeSeconds, 60);
    }

    public Double pace() {
        return divide(timeSeconds, divideBy(distance(), 1000));
    }
}
