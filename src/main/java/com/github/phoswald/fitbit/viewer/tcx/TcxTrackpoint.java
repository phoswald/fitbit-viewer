package com.github.phoswald.fitbit.viewer.tcx;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;

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

    public Double getLatitude() {
        return position == null ? null : position.getLatitudeDegrees();
    }

    public Double getLongitude() {
        return position == null ? null : position.getLongitudeDegrees();
    }

    public boolean hasAltitude() {
        return altitudeMeters != null;
    }

    public Double getAltitudeMeters() {
        return altitudeMeters;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public Double getDistanceKm() {
        return divideBy(distanceMeters, 1000);
    }

    public boolean hasHeartRate() {
        return heartRateBpm != null && heartRateBpm.getValue() != null;
    }

    public Integer getHeartRateBpm() {
        return heartRateBpm == null || heartRateBpm.getValue() == null ? null : heartRateBpm.getValue();
    }
}
