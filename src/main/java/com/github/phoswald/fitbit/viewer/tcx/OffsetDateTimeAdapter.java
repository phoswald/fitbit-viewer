package com.github.phoswald.fitbit.viewer.tcx;

import java.time.OffsetDateTime;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

class OffsetDateTimeAdapter extends XmlAdapter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime unmarshal(String value) {
        return value != null ? OffsetDateTime.parse(value) : null;
    }

    @Override
    public String marshal(OffsetDateTime value) {
        return value != null ? value.toString() : null;
    }
}
