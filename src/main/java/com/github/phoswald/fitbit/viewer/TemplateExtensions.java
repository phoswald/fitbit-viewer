package com.github.phoswald.fitbit.viewer;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.github.phoswald.fitbit.viewer.tcx.GeoPoint;

import io.quarkus.qute.RawString;
import io.quarkus.qute.TemplateExtension;

@TemplateExtension
class TemplateExtensions {

    static String format(ZonedDateTime dt, String pattern) {
        return dt.format(DateTimeFormatter.ofPattern(pattern));
    }

    static String format(OffsetDateTime dt, String pattern) {
        return dt.format(DateTimeFormatter.ofPattern(pattern));
    }

    static String format(Double value, String pattern) {
        return new DecimalFormat(pattern).format(value);
    }

    static String formatDuration(Double minutes) {
        var duration = Duration.ofSeconds((long) (minutes.doubleValue() * 60));
        if(duration.toHoursPart() > 0) {
            return String.format("%d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        } else {
            return String.format("%02d:%02d", duration.toMinutes(), duration.toSecondsPart());
        }
    }

    static String formatPace(Double seconds) {
        var d = Duration.ofSeconds(seconds.longValue());
        return String.format("%02d:%02d", d.toMinutesPart(), d.toSecondsPart());
    }

    static RawString formatJsGeoPoints(List<GeoPoint> points) {
        StringBuilder sb = new StringBuilder("[");
        for (GeoPoint point : points) {
            sb.append("[");
            sb.append(point.latitude());
            sb.append(",");
            sb.append(point.longitude());
            sb.append("],");
        }
        sb.append("]");
        return new RawString(sb.toString());
    }

    static RawString formatJsArray(List<?> list) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(Object element : list) {
            if(element instanceof Number) {
                builder.append(element);
            } else {
                builder.append("\"" + element + "\""); // TODO: escape correctly
            }
            builder.append(",");
        }
        builder.append("]");
        return new RawString(builder.toString());
    }
}
