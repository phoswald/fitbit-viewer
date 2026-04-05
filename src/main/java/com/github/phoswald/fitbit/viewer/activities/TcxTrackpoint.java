package com.github.phoswald.fitbit.viewer.activities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
class TcxTrackpoint {

    @XmlElement(name = "Position", namespace = TcxDatabase.NS)
    TcxPosition position;
}
