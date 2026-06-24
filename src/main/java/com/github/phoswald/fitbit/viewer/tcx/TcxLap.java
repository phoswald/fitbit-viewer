package com.github.phoswald.fitbit.viewer.tcx;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxLap {

    @XmlElement(name = "TotalTimeSeconds", namespace = TcxDatabase.NS)
    private Double totalTimeSeconds;

    @XmlElement(name = "DistanceMeters", namespace = TcxDatabase.NS)
    private Double distanceMeters;

    @XmlElementWrapper(name = "Track", namespace = TcxDatabase.NS)
    @XmlElement(name = "Trackpoint", namespace = TcxDatabase.NS)
    private List<TcxTrackpoint> trackpoints;

    public Double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public List<TcxTrackpoint> getTrackpoints() {
        return trackpoints;
    }
}
