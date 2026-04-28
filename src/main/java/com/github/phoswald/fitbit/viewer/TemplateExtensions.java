package com.github.phoswald.fitbit.viewer;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

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

    static RawString formatJson(Object object, int indent) {
        JsonbConfig config = new JsonbConfig().withFormatting(false);
        try (Jsonb jsonb = JsonbBuilder.create(config)) {
            String json = jsonb.toJson(object);
            var spaces = new StringBuilder();
            while(indent-- > 0) {
                spaces.append(' ');
            }
            return new RawString(json.replace("\n","\n" + spaces.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
