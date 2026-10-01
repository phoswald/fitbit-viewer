package com.github.phoswald.fitbit.viewer.tcx;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxLap {

    private transient Integer number;

    @XmlElement(name = "TotalTimeSeconds", namespace = TcxDatabase.NS)
    private Double totalTimeSeconds;

    @XmlElement(name = "DistanceMeters", namespace = TcxDatabase.NS)
    private Double distanceMeters;

    @XmlElementWrapper(name = "Track", namespace = TcxDatabase.NS)
    @XmlElement(name = "Trackpoint", namespace = TcxDatabase.NS)
    private List<TcxTrackpoint> trackpoints;

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }

    public Double getDistanceKm() {
        return divideBy(distanceMeters, 1000);
    }

    public Double getDurationMinutes() {
        return divideBy(totalTimeSeconds, 60);
    }

    public Double getPace() {
        return divideBy(totalTimeSeconds, getDistanceKm());
    }

    public List<TcxTrackpoint> getTrackpoints() {
        return trackpoints;
    }
}
