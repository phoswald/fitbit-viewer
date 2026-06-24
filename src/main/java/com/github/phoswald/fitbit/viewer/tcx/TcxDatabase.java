package com.github.phoswald.fitbit.viewer.tcx;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TrainingCenterDatabase", namespace = TcxDatabase.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class TcxDatabase {

    static final String NS = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2";

    @XmlElementWrapper(name = "Activities", namespace = NS)
    @XmlElement(name = "Activity", namespace = NS)
    private List<TcxActivity> activities;

    public List<TcxActivity> getActivities() {
        return activities;
    }

    public List<TcxTrackpoint> collectTrackPoints() {
        if (activities == null) {
            return List.of();
        }
        return activities.stream()
                .filter(activity -> activity.getLaps() != null)
                .flatMap(activity -> activity.getLaps().stream())
                .filter(lap -> lap.getTrackpoints() != null)
                .flatMap(lap -> lap.getTrackpoints().stream())
                .toList();
    }

    public List<GeoPoint> collectGeoPoints() {
        return collectTrackPoints().stream()
                .filter(trackpoint -> trackpoint.getPosition() != null && trackpoint.getPosition().getLatitudeDegrees() != null && trackpoint.getPosition().getLongitudeDegrees() != null)
                .map(trackpoint -> new GeoPoint(trackpoint.getPosition().getLatitudeDegrees(), trackpoint.getPosition().getLongitudeDegrees()))
                .toList();
    }

    public List<GeoLap> collectGeoLaps() {
        if (activities == null) {
            return List.of();
        }
        var laps = activities.stream()
                .filter(activity -> activity.getLaps() != null)
                .flatMap(activity -> activity.getLaps().stream())
                .toList();
        var result = new ArrayList<GeoLap>();
        int number = 1;
        for (var lap : laps) {
            Double distance = divideBy(lap.getDistanceMeters(), 1000);
            Double durationMinutes = divideBy(lap.getTotalTimeSeconds(), 60);
            Double pace = divideBy(lap.getTotalTimeSeconds(), distance);
            result.add(new GeoLap(number++, distance, durationMinutes, pace));
        }
        return result;
    }
}
