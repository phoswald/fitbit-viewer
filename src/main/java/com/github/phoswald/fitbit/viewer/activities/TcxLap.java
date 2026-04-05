package com.github.phoswald.fitbit.viewer.activities;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
class TcxLap {

    @XmlElementWrapper(name = "Track", namespace = TcxDatabase.NS)
    @XmlElement(name = "Trackpoint", namespace = TcxDatabase.NS)
    List<TcxTrackpoint> trackpoints;
}
