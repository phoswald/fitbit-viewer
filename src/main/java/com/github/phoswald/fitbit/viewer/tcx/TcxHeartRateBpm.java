package com.github.phoswald.fitbit.viewer.tcx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TcxHeartRateBpm {

    @XmlElement(name = "Value", namespace = TcxDatabase.NS)
    private Integer value;

    public Integer getValue() {
        return value;
    }
}
