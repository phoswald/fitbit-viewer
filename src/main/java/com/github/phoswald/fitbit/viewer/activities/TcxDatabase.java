package com.github.phoswald.fitbit.viewer.activities;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TrainingCenterDatabase", namespace = TcxDatabase.NS)
@XmlAccessorType(XmlAccessType.FIELD)
class TcxDatabase {

    static final String NS = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2";

    @XmlElementWrapper(name = "Activities", namespace = NS)
    @XmlElement(name = "Activity", namespace = NS)
    List<TcxActivity> activities;
}
