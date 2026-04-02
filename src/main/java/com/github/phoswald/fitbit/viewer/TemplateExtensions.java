package com.github.phoswald.fitbit.viewer;

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
