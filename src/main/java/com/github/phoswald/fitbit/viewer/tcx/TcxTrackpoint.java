package com.github.phoswald.fitbit.viewer.tcx;

import java.time.OffsetDateTime;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxTrackpoint {

    @XmlElement(name = "Time", namespace = TcxDatabase.NS)
    @XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
    private OffsetDateTime time;

    @XmlElement(name = "Position", namespace = TcxDatabase.NS)
    private TcxPosition position;

    @XmlElement(name = "AltitudeMeters", namespace = TcxDatabase.NS)
    private Double altitudeMeters;

    @XmlElement(name = "DistanceMeters", namespace = TcxDatabase.NS)
    private Double distanceMeters;

    @XmlElement(name = "HeartRateBpm", namespace = TcxDatabase.NS)
    private TcxHeartRateBpm heartRateBpm;

    public OffsetDateTime getTime() {
        return time;
    }

    public TcxPosition getPosition() {
        return position;
    }

    public Double getAltitudeMeters() {
        return altitudeMeters;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public TcxHeartRateBpm getHeartRateBpm() {
        return heartRateBpm;
    }
}
