package com.github.phoswald.fitbit.viewer.tcx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxTrackpoint {

    @XmlElement(name = "Position", namespace = TcxDatabase.NS)
    private TcxPosition position;

    public TcxPosition getPosition() {
        return position;
    }

    public void setPosition(TcxPosition position) {
        this.position = position;
    }
}
