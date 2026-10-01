package com.github.phoswald.fitbit.viewer.tcx;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public List<TcxLap> getAllLaps() {
        if (activities == null) {
            return List.of();
        }
        AtomicInteger nextNumber = new AtomicInteger(1);
        return activities.stream()
                .filter(activity -> activity.getLaps() != null)
                .flatMap(activity -> activity.getLaps().stream())
                .peek(activity -> activity.setNumber(nextNumber.getAndIncrement()))
                .toList();
    }

    public List<TcxTrackpoint> getAllTrackpoints() {
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
}
