package com.github.phoswald.fitbit.viewer.activities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
class TcxPosition {

    @XmlElement(name = "LatitudeDegrees", namespace = TcxDatabase.NS)
    Double latitudeDegrees;

    @XmlElement(name = "LongitudeDegrees", namespace = TcxDatabase.NS)
    Double longitudeDegrees;
}
