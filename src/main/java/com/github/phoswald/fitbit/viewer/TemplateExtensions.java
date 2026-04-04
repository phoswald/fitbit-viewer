package com.github.phoswald.fitbit.viewer;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
