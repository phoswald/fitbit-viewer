package com.github.phoswald.fitbit.viewer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
class TemplateExtensions {

    static String format(ZonedDateTime dt, String pattern) {
        return dt.format(DateTimeFormatter.ofPattern(pattern));
    }
}
