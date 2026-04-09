package com.github.phoswald.fitbit.viewer.tcx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxPosition {

    @XmlElement(name = "LatitudeDegrees", namespace = TcxDatabase.NS)
    private Double latitudeDegrees;

    @XmlElement(name = "LongitudeDegrees", namespace = TcxDatabase.NS)
    private Double longitudeDegrees;

    public Double getLatitudeDegrees() {
        return latitudeDegrees;
    }

    public Double getLongitudeDegrees() {
        return longitudeDegrees;
    }
}
