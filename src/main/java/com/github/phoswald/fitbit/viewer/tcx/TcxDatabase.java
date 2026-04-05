package com.github.phoswald.fitbit.viewer.tcx;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TrainingCenterDatabase", namespace = TcxDatabase.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class TcxDatabase {

    static final String NS = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2";

    @XmlElementWrapper(name = "Activities", namespace = NS)
    @XmlElement(name = "Activity", namespace = NS)
    private List<TcxActivity> activities;

    public List<TcxActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<TcxActivity> activities) {
        this.activities = activities;
    }
}
