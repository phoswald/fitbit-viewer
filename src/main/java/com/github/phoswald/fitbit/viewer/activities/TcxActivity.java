package com.github.phoswald.fitbit.viewer.activities;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
class TcxActivity {

    @XmlElement(name = "Lap", namespace = TcxDatabase.NS)
    List<TcxLap> laps;
}
