package com.github.phoswald.fitbit.viewer.tcx;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxLap {

    @XmlElementWrapper(name = "Track", namespace = TcxDatabase.NS)
    @XmlElement(name = "Trackpoint", namespace = TcxDatabase.NS)
    private List<TcxTrackpoint> trackpoints;

    public List<TcxTrackpoint> getTrackpoints() {
        return trackpoints;
    }

    public void setTrackpoints(List<TcxTrackpoint> trackpoints) {
        this.trackpoints = trackpoints;
    }
}
