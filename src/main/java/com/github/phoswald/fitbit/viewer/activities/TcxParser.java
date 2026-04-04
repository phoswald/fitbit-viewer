package com.github.phoswald.fitbit.viewer.activities;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

class TcxParser {

    static List<TrackPoint> parse(String xml) {
        var points = new ArrayList<TrackPoint>();
        try {
            var factory = XMLInputFactory.newInstance();
            var reader = factory.createXMLStreamReader(new StringReader(xml));
            boolean inPosition = false;
            boolean inLat = false;
            boolean inLon = false;
            Double lat = null;
            Double lon = null;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("Position".equals(name)) {
                        inPosition = true;
                        lat = null;
                        lon = null;
                    } else if (inPosition && "LatitudeDegrees".equals(name)) {
                        inLat = true;
                    } else if (inPosition && "LongitudeDegrees".equals(name)) {
                        inLon = true;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("Position".equals(name)) {
                        if (lat != null && lon != null) {
                            points.add(new TrackPoint(lat, lon));
                        }
                        inPosition = false;
                        inLat = false;
                        inLon = false;
                    } else if ("LatitudeDegrees".equals(name)) {
                        inLat = false;
                    } else if ("LongitudeDegrees".equals(name)) {
                        inLon = false;
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    if (inLat) {
                        lat = Double.parseDouble(reader.getText().trim());
                    } else if (inLon) {
                        lon = Double.parseDouble(reader.getText().trim());
                    }
                }
            }
        } catch (Exception ignored) {
            // Return whatever was collected; caller treats empty list as no GPS
        }
        return points;
    }
}
