package com.github.phoswald.fitbit.viewer.tcx;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxActivity {

    @XmlElement(name = "Lap", namespace = TcxDatabase.NS)
    private List<TcxLap> laps;

    public List<TcxLap> getLaps() {
        return laps;
    }

    public void setLaps(List<TcxLap> laps) {
        this.laps = laps;
    }
}
